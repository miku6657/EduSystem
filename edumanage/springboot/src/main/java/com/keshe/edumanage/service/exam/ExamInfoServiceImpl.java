package com.keshe.edumanage.service.exam;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.keshe.edumanage.common.exception.BusinessException;
import com.keshe.edumanage.entity.base.Classroom;
import com.keshe.edumanage.entity.exam.ExamInfo;
import com.keshe.edumanage.entity.exam.ExamMonitor;
import com.keshe.edumanage.entity.exam.ExamRoom;
import com.keshe.edumanage.mapper.exam.ExamInfoMapper;
import com.keshe.edumanage.service.base.ClassroomService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 考试信息业务实现
 */
@Service
@RequiredArgsConstructor
public class ExamInfoServiceImpl extends ServiceImpl<ExamInfoMapper, ExamInfo>
        implements ExamInfoService {

    /** 监考角色：主监考 */
    public static final String MONITOR_ROLE_MAIN = "MAIN";

    /** 监考角色：副监考 */
    public static final String MONITOR_ROLE_SUB = "SUB";

    private final ExamRoomService examRoomService;
    private final ExamMonitorService examMonitorService;
    private final ClassroomService classroomService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void arrangeExam(ExamInfo examInfo, List<Long> classroomIds,
                            List<Long> monitorTeacherIds) {
        if (classroomIds == null || classroomIds.isEmpty()) {
            throw new BusinessException("请至少选择一个考场教室");
        }
        if (monitorTeacherIds == null || monitorTeacherIds.isEmpty()) {
            throw new BusinessException("请至少安排一名监考教师");
        }
        // 冲突判断：教室、监考教师在该时间段不可重复使用
        checkClassroomConflict(examInfo, classroomIds);
        checkMonitorConflict(examInfo, monitorTeacherIds);

        examInfo.setId(null);
        examInfo.setStatus(STATUS_ARRANGED);
        save(examInfo);

        // 考场安排：座位数取教室容量
        List<Classroom> classrooms = classroomService.listByIds(classroomIds);
        if (classrooms.size() != classroomIds.size()) {
            throw new BusinessException("存在无效的教室信息");
        }
        List<ExamRoom> rooms = classrooms.stream().map(room -> {
            ExamRoom examRoom = new ExamRoom();
            examRoom.setExamId(examInfo.getId());
            examRoom.setClassroomId(room.getId());
            examRoom.setSeatCount(room.getCapacity());
            return examRoom;
        }).toList();
        examRoomService.saveBatch(rooms);

        // 监考安排：第一位主监考，其余副监考
        List<ExamMonitor> monitors = monitorTeacherIds.stream().map(teacherId -> {
            ExamMonitor monitor = new ExamMonitor();
            monitor.setExamId(examInfo.getId());
            monitor.setTeacherId(teacherId);
            monitor.setMonitorRole(teacherId.equals(monitorTeacherIds.get(0))
                    ? MONITOR_ROLE_MAIN : MONITOR_ROLE_SUB);
            return monitor;
        }).toList();
        examMonitorService.saveBatch(monitors);
    }

    @Override
    public Page<ExamInfo> pageExams(
            Page<ExamInfo> page,
            String name,
            Long termId,
            Long courseId,
            String examType
    ) {
        return lambdaQuery()
                .like(name != null && !name.isBlank(), ExamInfo::getName, name)
                .eq(termId != null, ExamInfo::getTermId, termId)
                .eq(examType != null && !examType.isBlank(), ExamInfo::getExamType, examType)
                .orderByDesc(ExamInfo::getExamDate)
                .page(page);
    }

    @Override
    public List<ExamInfo> listByTerm(Long termId) {
        return lambdaQuery()
                .eq(ExamInfo::getTermId, termId)
                .orderByAsc(ExamInfo::getExamDate)
                .list();
    }

    @Override
    public List<ExamInfo> listByCourse(Long courseId) {
        return lambdaQuery()
                .eq(ExamInfo::getCourseId, courseId)
                .orderByAsc(ExamInfo::getExamDate)
                .list();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void removeExam(Long id) {
        if (getById(id) == null) {
            throw new BusinessException("考试不存在");
        }
        removeById(id);
        examRoomService.removeByExam(id);
        examMonitorService.removeByExam(id);
    }

    /**
     * 教室冲突判断：与同一天其他时间重叠的考试，不得使用相同教室
     */
    private void checkClassroomConflict(ExamInfo examInfo, List<Long> classroomIds) {
        List<Long> overlappedExamIds = listOverlappedExamIds(examInfo);
        if (overlappedExamIds.isEmpty()) {
            return;
        }
        boolean conflict = examRoomService.lambdaQuery()
                .in(ExamRoom::getExamId, overlappedExamIds)
                .in(ExamRoom::getClassroomId, classroomIds)
                .exists();
        if (conflict) {
            throw new BusinessException("所选教室在该考试时间段内已被其他考试占用");
        }
    }

    /**
     * 监考冲突判断：同一位教师在时间重叠的考试中不得重复安排监考
     */
    private void checkMonitorConflict(ExamInfo examInfo, List<Long> monitorTeacherIds) {
        List<Long> overlappedExamIds = listOverlappedExamIds(examInfo);
        if (overlappedExamIds.isEmpty()) {
            return;
        }
        boolean conflict = examMonitorService.lambdaQuery()
                .in(ExamMonitor::getExamId, overlappedExamIds)
                .in(ExamMonitor::getTeacherId, monitorTeacherIds)
                .exists();
        if (conflict) {
            throw new BusinessException("监考教师在该考试时间段内已有监考安排");
        }
    }

    /**
     * 查询与给定考试同一天且时间段重叠的其他考试ID列表
     */
    private List<Long> listOverlappedExamIds(ExamInfo examInfo) {
        return lambdaQuery()
                .eq(ExamInfo::getExamDate, examInfo.getExamDate())
                .lt(ExamInfo::getStartTime, examInfo.getEndTime())
                .gt(ExamInfo::getEndTime, examInfo.getStartTime())
                .ne(examInfo.getId() != null, ExamInfo::getId, examInfo.getId())
                .list()
                .stream()
                .map(ExamInfo::getId)
                .toList();
    }
}
