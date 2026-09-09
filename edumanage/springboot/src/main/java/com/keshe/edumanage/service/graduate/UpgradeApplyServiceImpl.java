package com.keshe.edumanage.service.graduate;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.keshe.edumanage.common.exception.BusinessException;
import com.keshe.edumanage.entity.graduate.UpgradeApply;
import com.keshe.edumanage.mapper.graduate.UpgradeApplyMapper;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 专升本报名业务实现
 */
@Service
public class UpgradeApplyServiceImpl extends ServiceImpl<UpgradeApplyMapper, UpgradeApply>
        implements UpgradeApplyService {

    @Override
    public void apply(UpgradeApply upgradeApply) {
        if (upgradeApply.getStudentId() == null) {
            throw new BusinessException("学生信息不能为空");
        }
        // 已有未驳回的报名记录时不允许重复报名
        boolean exists = lambdaQuery()
                .eq(UpgradeApply::getStudentId, upgradeApply.getStudentId())
                .ne(UpgradeApply::getApplyStatus, STATUS_FAIL)
                .exists();
        if (exists) {
            throw new BusinessException("已有专升本报名记录，请勿重复报名");
        }
        upgradeApply.setId(null);
        upgradeApply.setApplyStatus(STATUS_WAIT);
        save(upgradeApply);
    }

    @Override
    public void audit(Long id, String status) {
        if (!STATUS_PASS.equals(status) && !STATUS_FAIL.equals(status)) {
            throw new BusinessException("审核结果只能是 PASS 或 FAIL");
        }
        UpgradeApply apply = getById(id);
        if (apply == null) {
            throw new BusinessException("报名记录不存在");
        }
        if (!STATUS_WAIT.equals(apply.getApplyStatus())) {
            throw new BusinessException("该报名已审核，不能重复审核");
        }
        apply.setApplyStatus(status);
        updateById(apply);
    }

    @Override
    public List<UpgradeApply> listForReport(String applyStatus) {
        return lambdaQuery()
                .eq(applyStatus != null && !applyStatus.isBlank(),
                        UpgradeApply::getApplyStatus, applyStatus)
                .orderByDesc(UpgradeApply::getId)
                .list();
    }
}
