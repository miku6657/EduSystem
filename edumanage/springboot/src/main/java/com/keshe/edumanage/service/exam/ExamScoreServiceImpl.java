package com.keshe.edumanage.service.exam;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.keshe.edumanage.common.exception.BusinessException;
import com.keshe.edumanage.entity.exam.ExamScore;
import com.keshe.edumanage.mapper.exam.ExamScoreMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 考试成绩业务实现
 */
@Service
public class ExamScoreServiceImpl extends ServiceImpl<ExamScoreMapper, ExamScore>
        implements ExamScoreService {

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveScores(Long examId, List<ExamScore> scores) {
        for (ExamScore score : scores) {
            ExamScore exist = lambdaQuery()
                    .eq(ExamScore::getExamId, examId)
                    .eq(ExamScore::getStudentId, score.getStudentId())
                    .one();
            if (exist == null) {
                score.setId(null);
                score.setExamId(examId);
                save(score);
            } else {
                exist.setScore(score.getScore());
                exist.setStatus(score.getStatus());
                updateById(exist);
            }
        }
    }

    @Override
    public Page<ExamScore> pageByExam(Page<ExamScore> page, Long examId, String status) {
        return lambdaQuery()
                .eq(ExamScore::getExamId, examId)
                .eq(status != null && !status.isBlank(), ExamScore::getStatus, status)
                .orderByDesc(ExamScore::getId)
                .page(page);
    }

    @Override
    public List<ExamScore> listByStudent(Long studentId) {
        return lambdaQuery()
                .eq(ExamScore::getStudentId, studentId)
                .list();
    }

    @Override
    public Map<String, Object> statByExam(Long examId) {
        List<ExamScore> scores = lambdaQuery()
                .eq(ExamScore::getExamId, examId)
                .list();
        long total = scores.size();
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
        result.put("total", total);              // 应考人数
        result.put("actual", total - absent);    // 实考人数
        result.put("absent", absent);            // 缺考人数
        result.put("passed", passed);            // 及格人数
        result.put("failed", failed);            // 不及格人数
        return result;
    }
}
