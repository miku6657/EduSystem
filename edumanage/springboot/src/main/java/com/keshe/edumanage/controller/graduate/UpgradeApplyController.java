package com.keshe.edumanage.controller.graduate;

import com.keshe.edumanage.common.result.Result;
import com.keshe.edumanage.entity.graduate.UpgradeApply;
import com.keshe.edumanage.service.graduate.UpgradeApplyService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 专升本报名接口
 *
 * 学生报名
 *      ↓
 * 审核
 *      ↓
 * 报表查询
 */
@RestController
@RequestMapping("/api/upgrade-applies")
@RequiredArgsConstructor
public class UpgradeApplyController {

    private final UpgradeApplyService upgradeApplyService;


    /**
     * 提交专升本报名
     *
     * POST /api/upgrade-applies
     */
    @PostMapping
    public Result<Void> apply(
            @RequestBody UpgradeApply upgradeApply
    ) {
        upgradeApplyService.apply(upgradeApply);
        return Result.success();
    }


    /**
     * 审核报名
     *
     * PUT /api/upgrade-applies/{id}/audit
     *
     * status:
     * PASS 通过
     * FAIL 驳回
     */
    @PutMapping("/{id}/audit")
    public Result<Void> audit(
            @PathVariable Long id,
            @RequestParam String status
    ) {
        upgradeApplyService.audit(
                id,
                status
        );

        return Result.success();
    }


    /**
     * 报名报表查询
     *
     * GET /api/upgrade-applies/report
     */
    @GetMapping("/report")
    public Result<List<UpgradeApply>> reportList(
            @RequestParam(required = false) String applyStatus
    ) {
        return Result.success(
                upgradeApplyService.listForReport(
                        applyStatus
                )
        );
    }


    /**
     * 查询报名详情
     *
     * GET /api/upgrade-applies/{id}
     */
    @GetMapping("/{id}")
    public Result<UpgradeApply> getById(
            @PathVariable Long id
    ) {
        return Result.success(
                upgradeApplyService.getById(id)
        );
    }
}
