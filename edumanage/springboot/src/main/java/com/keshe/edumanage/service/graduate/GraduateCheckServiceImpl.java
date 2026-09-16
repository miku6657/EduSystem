package com.keshe.edumanage.service.graduate;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.keshe.edumanage.common.exception.BusinessException;
import com.keshe.edumanage.entity.exam.ExamScore;
import com.keshe.edumanage.entity.graduate.GraduateCheck;
import com.keshe.edumanage.mapper.graduate.GraduateCheckMapper;
import com.keshe.edumanage.service.exam.ExamScoreService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 毕业资格审核业务实现
 */
@Service
public class GraduateCheckServiceImpl extends ServiceImpl<GraduateCheckMapper, GraduateCheck>
        implements GraduateCheckService {

    private final ExamScoreService examScoreService;

    public GraduateCheckServiceImpl(ExamScoreService examScoreService) {
        this.examScoreService = examScoreService;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void audit(GraduateCheck check) {
        if (check.getStudentId() == null) {
            throw new BusinessException("学生信息不能为空");
        }

        List<ExamScore> scores = examScoreService.listByStudent(check.getStudentId());

        boolean hasFailedScore = scores.stream()
                .anyMatch(score ->
                        score.getScore() != null
                                && score.getScore() < ExamScoreService.PASS_SCORE
                );

        String result = hasFailedScore ? STATUS_FAIL : STATUS_PASS;

        check.setCheckStatus(result);
        check.setCourseStatus(result);
        check.setCreditStatus(result);

        if (hasFailedScore) {
            check.setRemark("存在低于60分的成绩，毕业审核不通过");
        } else if (check.getRemark() == null || check.getRemark().isBlank()) {
            check.setRemark("所有已录入成绩均不低于60分，毕业审核通过");
        }

        GraduateCheck exist = getByStudent(check.getStudentId());

        if (exist != null) {
            check.setId(exist.getId());
        }

        saveOrUpdate(check);
    }

    @Override
    public List<GraduateCheck> listByStatus(String checkStatus) {
        return lambdaQuery()
                .eq(checkStatus != null && !checkStatus.isBlank(),
                        GraduateCheck::getCheckStatus, checkStatus)
                .orderByDesc(GraduateCheck::getId)
                .list();
    }

    @Override
    public GraduateCheck getByStudent(Long studentId) {
        return lambdaQuery()
                .eq(GraduateCheck::getStudentId, studentId)
                .one();
    }
}
