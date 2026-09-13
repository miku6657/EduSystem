package com.keshe.edumanage.service.exam;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.keshe.edumanage.entity.base.Classroom;
import com.keshe.edumanage.entity.base.Teacher;
import com.keshe.edumanage.entity.exam.ExamInfo;
import com.keshe.edumanage.entity.exam.ExamMonitor;
import com.keshe.edumanage.entity.exam.ExamRoom;
import com.keshe.edumanage.mapper.base.ClassroomMapper;
import com.keshe.edumanage.mapper.base.TeacherMapper;
import com.keshe.edumanage.mapper.exam.ExamInfoMapper;
import com.keshe.edumanage.mapper.exam.ExamMonitorMapper;
import com.keshe.edumanage.mapper.exam.ExamRoomMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 监考安排业务实现
 * <p>注意：这里刻意注入 Mapper 而不是 ExamInfoService —— ExamInfoServiceImpl 依赖本服务，
 * 若本服务再依赖 ExamInfoService 会形成构造器循环依赖，Spring 启动即失败。</p>
 */
@Service
@RequiredArgsConstructor
public class ExamMonitorServiceImpl extends ServiceImpl<ExamMonitorMapper, ExamMonitor>
        implements ExamMonitorService {

    private final ExamInfoMapper examInfoMapper;
    private final ExamRoomMapper examRoomMapper;
    private final ClassroomMapper classroomMapper;
    private final TeacherMapper teacherMapper;

    @Override
    public List<ExamMonitor> listByExam(Long examId) {
        List<ExamMonitor> monitors = lambdaQuery()
                .eq(ExamMonitor::getExamId, examId)
                .list();
        enrich(monitors);
        return monitors;
    }

    @Override
    public List<ExamMonitor> listByTeacher(Long teacherId) {
        List<ExamMonitor> monitors = lambdaQuery()
                .eq(ExamMonitor::getTeacherId, teacherId)
                .list();
        enrich(monitors);
        return monitors;
    }

    @Override
    public void removeByExam(Long examId) {
        lambdaUpdate()
                .eq(ExamMonitor::getExamId, examId)
                .remove();
    }

    /**
     * 填充展示字段：考试名称/课程/日期时间、考场名称、教师姓名
     */
    private void enrich(List<ExamMonitor> monitors) {
        if (monitors == null || monitors.isEmpty()) {
            return;
        }
        List<Long> examIds = monitors.stream()
                .map(ExamMonitor::getExamId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        Map<Long, ExamInfo> examMap = examIds.isEmpty()
                ? Map.of()
                : examInfoMapper.selectBatchIds(examIds).stream()
                        .collect(Collectors.toMap(ExamInfo::getId, Function.identity(), (a, b) -> a));

        // 考场：exam_room（考试-教室）→ base_classroom（教室名称），同一场考试可能多个考场
        Map<Long, List<ExamRoom>> roomMap = examIds.isEmpty()
                ? Map.of()
                : examRoomMapper.selectList(null).stream()
                        .filter(room -> examIds.contains(room.getExamId()))
                        .collect(Collectors.groupingBy(ExamRoom::getExamId));
        List<Long> classroomIds = roomMap.values().stream()
                .flatMap(List::stream)
                .map(ExamRoom::getClassroomId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        Map<Long, Classroom> classroomMap = classroomIds.isEmpty()
                ? Map.of()
                : classroomMapper.selectBatchIds(classroomIds).stream()
                        .collect(Collectors.toMap(Classroom::getId, Function.identity(), (a, b) -> a));

        List<Long> teacherIds = monitors.stream()
                .map(ExamMonitor::getTeacherId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        Map<Long, Teacher> teacherMap = teacherIds.isEmpty()
                ? Map.of()
                : teacherMapper.selectBatchIds(teacherIds).stream()
                        .collect(Collectors.toMap(Teacher::getId, Function.identity(), (a, b) -> a));

        for (ExamMonitor monitor : monitors) {
            ExamInfo exam = examMap.get(monitor.getExamId());
            if (exam != null) {
                monitor.setExamName(exam.getName());
                monitor.setExamDate(exam.getExamDate());
                monitor.setStartTime(exam.getStartTime());
                monitor.setEndTime(exam.getEndTime());
            }
            List<ExamRoom> rooms = roomMap.get(monitor.getExamId());
            if (rooms != null && !rooms.isEmpty()) {
                monitor.setRoomName(rooms.stream()
                        .map(room -> classroomMap.get(room.getClassroomId()))
                        .filter(Objects::nonNull)
                        .map(Classroom::getRoomNo)
                        .collect(Collectors.joining("、")));
            }
            Teacher teacher = teacherMap.get(monitor.getTeacherId());
            if (teacher != null) {
                monitor.setTeacherName(teacher.getName());
            }
        }
    }
}
