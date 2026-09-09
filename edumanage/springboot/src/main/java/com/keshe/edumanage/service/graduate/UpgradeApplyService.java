package com.keshe.edumanage.service.graduate;

import com.baomidou.mybatisplus.extension.service.IService;
import com.keshe.edumanage.entity.graduate.UpgradeApply;

import java.util.List;

/**
 * 专升本报名业务接口
 * <p>学生报名、审核报名资格、生成报名报表</p>
 */
public interface UpgradeApplyService extends IService<UpgradeApply> {

    /** 报名状态：待审核 */
    String STATUS_WAIT = "WAIT";

    /** 报名状态：审核通过 */
    String STATUS_PASS = "PASS";

    /** 报名状态：审核驳回 */
    String STATUS_FAIL = "FAIL";

    /**
     * 学生提交专升本报名（已有未驳回的报名记录时不允许重复报名）
     *
     * @param upgradeApply 报名信息（学生ID、报考院校、报考专业）
     */
    void apply(UpgradeApply upgradeApply);

    /**
     * 审核报名（仅待审核状态可审核）
     *
     * @param id     报名记录ID
     * @param status 审核结果：PASS / FAIL
     */
    void audit(Long id, String status);

    /**
     * 按状态查询报名列表（用于生成报表）
     *
     * @param applyStatus 报名状态（为空时查全部）
     * @return 报名列表
     */
    List<UpgradeApply> listForReport(String applyStatus);
}
