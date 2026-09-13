package com.keshe.edumanage.service.exam;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.keshe.edumanage.common.exception.BusinessException;
import com.keshe.edumanage.entity.base.Course;
import com.keshe.edumanage.entity.base.Teacher;
import com.keshe.edumanage.entity.exam.ExamApply;
import com.keshe.edumanage.mapper.exam.ExamApplyMapper;
import com.keshe.edumanage.service.base.CourseService;
import com.keshe.edumanage.service.base.TeacherService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 考核方式申报业务实现
 */
@Service
@RequiredArgsConstructor
public class ExamApplyServiceImpl extends ServiceImpl<ExamApplyMapper, ExamApply>
        implements ExamApplyService {

    private final CourseService courseService;
    private final TeacherService teacherService;

    @Override
    public void apply(ExamApply examApply) {
        if (examApply.getCourseId() == null) {
            throw new BusinessException("请选择申报课程");
        }
        if (examApply.getApplyType() == null || examApply.getApplyType().isBlank()) {
            throw new BusinessException("请选择考核方式");
        }
        boolean exists = lambdaQuery()
                .eq(ExamApply::getCourseId, examApply.getCourseId())
                .eq(ExamApply::getTeacherId, examApply.getTeacherId())
                .eq(ExamApply::getStatus, STATUS_WAIT)
                .exists();
        if (exists) {
            throw new BusinessException("该课程已有待审核的考核方式申报，请勿重复提交");
        }
        examApply.setId(null);
        examApply.setStatus(STATUS_WAIT);
        save(examApply);
    }

    @Override
    public void audit(Long id, String status) {
        if (!STATUS_PASS.equals(status) && !STATUS_FAIL.equals(status)) {
            throw new BusinessException("审核结果只能是 PASS 或 FAIL");
        }
        ExamApply apply = getById(id);
        if (apply == null) {
            throw new BusinessException("申报记录不存在");
        }
        if (!STATUS_WAIT.equals(apply.getStatus())) {
            throw new BusinessException("该申报已审核，不能重复审核");
        }
        apply.setStatus(status);
        updateById(apply);
    }

    @Override
    public List<ExamApply> listForExport(String status) {
        List<ExamApply> list = lambdaQuery()
                .eq(status != null && !status.isBlank(), ExamApply::getStatus, status)
                .orderByDesc(ExamApply::getId)
                .list();
        enrich(list);
        return list;
    }

    @Override
    public List<ExamApply> listByTeacher(Long teacherId) {
        List<ExamApply> list = lambdaQuery()
                .eq(ExamApply::getTeacherId, teacherId)
                .orderByDesc(ExamApply::getId)
                .list();
        enrich(list);
        return list;
    }

    /**
     * 填充展示字段：课程名称、教师姓名
     */
    private void enrich(List<ExamApply> list) {
        if (list == null || list.isEmpty()) {
            return;
        }
        List<Long> courseIds = list.stream()
                .map(ExamApply::getCourseId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        Map<Long, Course> courseMap = courseIds.isEmpty()
                ? Map.of()
                : courseService.listByIds(courseIds).stream()
                        .collect(Collectors.toMap(Course::getId, Function.identity(), (a, b) -> a));

        List<Long> teacherIds = list.stream()
                .map(ExamApply::getTeacherId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        Map<Long, Teacher> teacherMap = teacherIds.isEmpty()
                ? Map.of()
                : teacherService.listByIds(teacherIds).stream()
                        .collect(Collectors.toMap(Teacher::getId, Function.identity(), (a, b) -> a));

        for (ExamApply apply : list) {
            Course course = courseMap.get(apply.getCourseId());
            if (course != null) {
                apply.setCourseName(course.getName());
            }
            Teacher teacher = teacherMap.get(apply.getTeacherId());
            if (teacher != null) {
                apply.setTeacherName(teacher.getName());
            }
        }
    }
}
