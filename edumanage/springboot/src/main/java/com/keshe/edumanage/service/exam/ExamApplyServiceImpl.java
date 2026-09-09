package com.keshe.edumanage.service.exam;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.keshe.edumanage.common.exception.BusinessException;
import com.keshe.edumanage.entity.exam.ExamApply;
import com.keshe.edumanage.mapper.exam.ExamApplyMapper;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 考核方式申报业务实现
 */
@Service
public class ExamApplyServiceImpl extends ServiceImpl<ExamApplyMapper, ExamApply>
        implements ExamApplyService {

    @Override
    public void apply(ExamApply examApply) {
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
        return lambdaQuery()
                .eq(status != null && !status.isBlank(), ExamApply::getStatus, status)
                .orderByDesc(ExamApply::getId)
                .list();
    }
}
