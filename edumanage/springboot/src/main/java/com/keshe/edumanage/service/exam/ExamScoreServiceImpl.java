package com.keshe.edumanage.service.exam;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.keshe.edumanage.common.exception.BusinessException;
import com.keshe.edumanage.entity.base.Course;
import com.keshe.edumanage.entity.base.Student;
import com.keshe.edumanage.entity.exam.ExamInfo;
import com.keshe.edumanage.entity.exam.ExamScore;
import com.keshe.edumanage.mapper.exam.ExamScoreMapper;
import com.keshe.edumanage.service.base.CourseService;
import com.keshe.edumanage.service.base.StudentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 考试成绩业务实现
 */
@Service
@RequiredArgsConstructor
public class ExamScoreServiceImpl extends ServiceImpl<ExamScoreMapper, ExamScore>
        implements ExamScoreService {

    private final ExamInfoService examInfoService;
    private final CourseService courseService;
    private final StudentService studentService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveScores(Long examId, List<ExamScore> scores) {
        if (scores == null || scores.isEmpty()) {
            throw new BusinessException("没有需要保存的成绩记录");
        }
        if (examInfoService.getById(examId) == null) {
            throw new BusinessException("考试信息不存在");
        }
        for (ExamScore score : scores) {
            if (score.getStudentId() == null) {
                throw new BusinessException("成绩记录缺少学生ID");
            }
            if (score.getScore() != null && (score.getScore() < 0 || score.getScore() > 100)) {
                throw new BusinessException("分数必须在 0~100 之间");
            }
            ExamScore exist = lambdaQuery()
                    .eq(ExamScore::getExamId, examId)
                    .eq(ExamScore::getStudentId, score.getStudentId())
                    .one();
            if (exist == null) {
                score.setId(null);
                score.setExamId(examId);
                score.setStatus(score.getStatus() == null
                        ? (score.getScore() == null ? STATUS_ABSENT : STATUS_NORMAL)
                        : score.getStatus());
                save(score);
            } else {
                exist.setScore(score.getScore());
                exist.setStatus(score.getStatus() == null ? exist.getStatus() : score.getStatus());
                updateById(exist);
            }
        }
    }

    @Override
    public Page<ExamScore> pageByExam(Page<ExamScore> page, Long examId, String status) {
        Page<ExamScore> result = lambdaQuery()
                .eq(ExamScore::getExamId, examId)
                .eq(status != null && !status.isBlank(), ExamScore::getStatus, status)
                .orderByDesc(ExamScore::getId)
                .page(page);
        enrich(result.getRecords());
        return result;
    }

    @Override
    public List<ExamScore> listByStudent(Long studentId) {
        List<ExamScore> scores = lambdaQuery()
                .eq(ExamScore::getStudentId, studentId)
                .orderByDesc(ExamScore::getId)
                .list();
        enrich(scores);
        return scores;
    }

    @Override
    public Map<String, Object> statByExam(Long examId) {
        List<ExamScore> scores = lambdaQuery()
                .eq(ExamScore::getExamId, examId)
                .list();
        long recorded = scores.size();
        long absent = scores.stream()
                .filter(s -> STATUS_ABSENT.equals(s.getStatus()))
                .count();
        long passed = scores.stream()
                .filter(s -> s.getScore() != null && s.getScore() >= PASS_SCORE)
                .count();
        long failed = scores.stream()
                .filter(s -> s.getScore() != null && s.getScore() < PASS_SCORE)
                .count();

        Map<String, Object> result = new HashMap<>();
        // 统计口径：以已录入的成绩记录为基数（total 即"已录入人数"）
        result.put("total", recorded);
        result.put("actual", recorded - absent);
        result.put("absent", absent);
        result.put("passed", passed);
        result.put("failed", failed);
        return result;
    }

    /**
     * 填充展示字段：学生姓名/学号、考试名称、课程名称/学分、考试日期。
     * <p>用三次批量查询代替逐行反查，避免 N+1。</p>
     */
    private void enrich(List<ExamScore> scores) {
        if (scores == null || scores.isEmpty()) {
            return;
        }
        List<Long> examIds = scores.stream()
                .map(ExamScore::getExamId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        Map<Long, ExamInfo> examMap = examIds.isEmpty()
                ? Map.of()
                : examInfoService.listByIds(examIds).stream()
                        .collect(Collectors.toMap(ExamInfo::getId, Function.identity(), (a, b) -> a));

        List<Long> courseIds = examMap.values().stream()
                .map(ExamInfo::getCourseId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        Map<Long, Course> courseMap = courseIds.isEmpty()
                ? Map.of()
                : courseService.listByIds(courseIds).stream()
                        .collect(Collectors.toMap(Course::getId, Function.identity(), (a, b) -> a));

        List<Long> studentIds = scores.stream()
                .map(ExamScore::getStudentId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        Map<Long, Student> studentMap = studentIds.isEmpty()
                ? Map.of()
                : studentService.listByIds(studentIds).stream()
                        .collect(Collectors.toMap(Student::getId, Function.identity(), (a, b) -> a));

        for (ExamScore score : scores) {
            ExamInfo exam = examMap.get(score.getExamId());
            if (exam != null) {
                score.setExamName(exam.getName());
                score.setExamDate(exam.getExamDate());
                Course course = courseMap.get(exam.getCourseId());
                if (course != null) {
                    score.setCourseName(course.getName());
                    score.setCredit(course.getCredit());
                }
            }
            Student student = studentMap.get(score.getStudentId());
            if (student != null) {
                score.setStudentName(student.getName());
                score.setStudentNo(student.getStudentNo());
            }
        }
    }
}
