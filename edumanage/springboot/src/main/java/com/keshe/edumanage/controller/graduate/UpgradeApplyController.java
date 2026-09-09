package com.keshe.edumanage.controller.graduate;

import com.keshe.edumanage.common.result.Result;
import com.keshe.edumanage.entity.graduate.UpgradeApply;
import com.keshe.edumanage.service.graduate.UpgradeApplyService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 专升本报名接口
 */
@RestController
@RequestMapping("/api/upgrade-apply")
@RequiredArgsConstructor
public class UpgradeApplyController {

    private final UpgradeApplyService upgradeApplyService;

    /**
     * 学生提交专升本报名
     */
    @PostMapping("/apply")
    public Result<Void> apply(@RequestBody UpgradeApply upgradeApply) {
        upgradeApplyService.apply(upgradeApply);
        return Result.success();
    }

    /**
     * 审核报名（status：PASS 通过 / FAIL 驳回）
     */
    @PutMapping("/audit/{id}")
    public Result<Void> audit(@PathVariable Long id, @RequestParam String status) {
        upgradeApplyService.audit(id, status);
        return Result.success();
    }

    /**
     * 报名报表查询（applyStatus 为空查全部）
     */
    @GetMapping("/report-list")
    public Result<List<UpgradeApply>> reportList(@RequestParam(required = false) String applyStatus) {
        return Result.success(upgradeApplyService.listForReport(applyStatus));
    }

    /**
     * 根据ID查询报名详情
     */
    @GetMapping("/{id}")
    public Result<UpgradeApply> getById(@PathVariable Long id) {
        return Result.success(upgradeApplyService.getById(id));
    }
}
