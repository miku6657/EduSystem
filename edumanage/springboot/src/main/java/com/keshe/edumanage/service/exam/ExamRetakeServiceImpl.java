package com.keshe.edumanage.service.exam;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.keshe.edumanage.common.exception.BusinessException;
import com.keshe.edumanage.entity.base.Course;
import com.keshe.edumanage.entity.base.Student;
import com.keshe.edumanage.entity.exam.ExamInfo;
import com.keshe.edumanage.entity.exam.ExamRetake;
import com.keshe.edumanage.entity.exam.ExamScore;
import com.keshe.edumanage.mapper.exam.ExamRetakeMapper;
import com.keshe.edumanage.service.base.CourseService;
import com.keshe.edumanage.service.base.StudentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 补考重修业务实现
 */
@Service
@RequiredArgsConstructor
public class ExamRetakeServiceImpl extends ServiceImpl<ExamRetakeMapper, ExamRetake>
        implements ExamRetakeService {

    private final ExamInfoService examInfoService;
    private final ExamScoreService examScoreService;
    private final CourseService courseService;
    private final StudentService studentService;

    @Override
    public void applyRetake(Long studentId, Long courseId, String type) {
        if (studentId == null || courseId == null) {
            throw new BusinessException("请选择要申请的课程");
        }
        // 类型：补考 / 重修；为空时按重修处理（保持与原行为兼容）
        String retakeType = (type == null || type.isBlank()) ? TYPE_RETAKE : type.trim();
        if (!TYPE_MAKEUP.equals(retakeType) && !TYPE_RETAKE.equals(retakeType)) {
            throw new BusinessException("申请类型只能是补考或重修");
        }
        boolean exists = lambdaQuery()
                .eq(ExamRetake::getStudentId, studentId)
                .eq(ExamRetake::getCourseId, courseId)
                .isNull(ExamRetake::getExamId)
                .exists();
        if (exists) {
            throw new BusinessException("该课程已有待安排的申请，请勿重复提交");
        }
        // 校验课程历史最高成绩，及格则不允许申请
        Double bestScore = getBestScore(studentId, courseId);
        if (bestScore == null) {
            throw new BusinessException("该课程暂无考试成绩记录");
        }
        if (bestScore >= examScoreService.PASS_SCORE) {
            throw new BusinessException("该课程成绩已及格，无需" + retakeType);
        }

        ExamRetake retake = new ExamRetake();
        retake.setStudentId(studentId);
        retake.setCourseId(courseId);
        retake.setType(retakeType);
        save(retake);
    }

    @Override
    public void assignExam(Long id, Long examId) {
        ExamRetake retake = getById(id);
        if (retake == null) {
            throw new BusinessException("补考重修记录不存在");
        }
        if (retake.getExamId() != null) {
            throw new BusinessException("该记录已安排考试，不能重复安排");
        }
        if (examInfoService.getById(examId) == null) {
            throw new BusinessException("考试信息不存在");
        }
        retake.setExamId(examId);
        updateById(retake);
    }

    @Override
    public List<ExamRetake> listByStudent(Long studentId) {
        List<ExamRetake> retakes = lambdaQuery()
                .eq(ExamRetake::getStudentId, studentId)
                .orderByDesc(ExamRetake::getId)
                .list();
        enrich(retakes);
        return retakes;
    }

    @Override
    public List<ExamRetake> listByType(String type) {
        List<ExamRetake> retakes = lambdaQuery()
                .eq(ExamRetake::getType, type)
                .orderByDesc(ExamRetake::getId)
                .list();
        enrich(retakes);
        return retakes;
    }

    /**
     * 查询学生在某门课程的历史最高分
     * <p>成绩表通过考试关联课程，先取该课程的全部考试，再查成绩取最高分</p>
     */
    private Double getBestScore(Long studentId, Long courseId) {
        List<Long> examIds = examInfoService.listByCourse(courseId)
                .stream()
                .map(ExamInfo::getId)
                .toList();
        if (examIds.isEmpty()) {
            return null;
        }
        return examScoreService.lambdaQuery()
                .in(ExamScore::getExamId, examIds)
                .eq(ExamScore::getStudentId, studentId)
                .list()
                .stream()
                .map(ExamScore::getScore)
                .filter(Objects::nonNull)
                .max(Double::compare)
                .orElse(null);
    }

    /**
     * 填充展示字段：课程名称、学生姓名/学号、已安排场次的考试名称
     */
    private void enrich(List<ExamRetake> retakes) {
        if (retakes == null || retakes.isEmpty()) {
            return;
        }
        List<Long> courseIds = retakes.stream()
                .map(ExamRetake::getCourseId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        Map<Long, Course> courseMap = courseIds.isEmpty()
                ? Map.of()
                : courseService.listByIds(courseIds).stream()
                        .collect(Collectors.toMap(Course::getId, Function.identity(), (a, b) -> a));

        List<Long> studentIds = retakes.stream()
                .map(ExamRetake::getStudentId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        Map<Long, Student> studentMap = studentIds.isEmpty()
                ? Map.of()
                : studentService.listByIds(studentIds).stream()
                        .collect(Collectors.toMap(Student::getId, Function.identity(), (a, b) -> a));

        List<Long> examIds = retakes.stream()
                .map(ExamRetake::getExamId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        Map<Long, ExamInfo> examMap = examIds.isEmpty()
                ? Map.of()
                : examInfoService.listByIds(examIds).stream()
                        .collect(Collectors.toMap(ExamInfo::getId, Function.identity(), (a, b) -> a));

        for (ExamRetake retake : retakes) {
            Course course = courseMap.get(retake.getCourseId());
            if (course != null) {
                retake.setCourseName(course.getName());
            }
            Student student = studentMap.get(retake.getStudentId());
            if (student != null) {
                retake.setStudentName(student.getName());
                retake.setStudentNo(student.getStudentNo());
            }
            ExamInfo exam = examMap.get(retake.getExamId());
            if (exam != null) {
                retake.setExamName(exam.getName());
            }
        }
    }
}
