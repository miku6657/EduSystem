package com.keshe.edumanage.controller.base;

import com.keshe.edumanage.common.result.Result;
import com.keshe.edumanage.entity.base.ClassroomApply;
import com.keshe.edumanage.service.base.ClassroomApplyService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/classroom-applies")
@RequiredArgsConstructor
public class ClassroomApplyController {

    private final ClassroomApplyService classroomApplyService;

    /**
     * 提交教室申请
     */
    @PostMapping
    public Result<Void> apply(
            @RequestBody ClassroomApply apply,
            Authentication authentication
    ) {
        boolean conflict =
                classroomApplyService.checkConflict(
                        apply.getRoomId(),
                        apply.getApplyDate(),
                        apply.getTimeSlot()
                );

        if (conflict) {
            return Result.fail(
                    "该时间段教室已被占用"
            );
        }

        // 申请人一律以当前登录人为准，不信任请求体（否则可冒名提交）
        apply.setId(null);
        apply.setApplicant(
                authentication.getName()
        );
        apply.setStatus(
                ClassroomApplyService.STATUS_WAIT
        );

        classroomApplyService.save(apply);

        return Result.success();
    }

    /**
     * 我的申请
     */
    @GetMapping("/my")
    public Result<List<ClassroomApply>> myList(
            Authentication authentication
    ) {
        return Result.success(
                classroomApplyService.listByApplicant(
                        authentication.getName()
                )
        );
    }

    /**
     * 审批列表（status 为空查全部）
     */
    @GetMapping
    public Result<List<ClassroomApply>> list(
            @RequestParam(required = false) String status
    ) {
        return Result.success(
                classroomApplyService.listByStatus(
                        status
                )
        );
    }

    /**
     * 撤回我的申请（仅本人、仅"待审核"可撤回）
     * PUT /api/classroom-applies/{id}/cancel
     */
    @PutMapping("/{id}/cancel")
    public Result<Void> cancel(
            @PathVariable Long id,
            Authentication authentication
    ) {
        classroomApplyService.cancel(
                id,
                authentication.getName()
        );

        return Result.success();
    }

    /**
     * 通过申请（管理端）
     */
    @PutMapping("/{id}/approve")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> approve(
            @PathVariable Long id
    ) {
        ClassroomApply apply =
                classroomApplyService.getById(id);

        if (apply == null) {
            return Result.fail(
                    "申请记录不存在"
            );
        }

        if (!ClassroomApplyService.STATUS_WAIT.equals(apply.getStatus())) {
            return Result.fail(
                    "该申请已审批，请勿重复操作"
            );
        }

        apply.setStatus(
                ClassroomApplyService.STATUS_PASS
        );

        classroomApplyService.updateById(apply);

        return Result.success();
    }

    /**
     * 驳回申请（管理端）
     */
    @PutMapping("/{id}/reject")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> reject(
            @PathVariable Long id
    ) {
        ClassroomApply apply =
                classroomApplyService.getById(id);

        if (apply == null) {
            return Result.fail(
                    "申请记录不存在"
            );
        }

        if (!ClassroomApplyService.STATUS_WAIT.equals(apply.getStatus())) {
            return Result.fail(
                    "该申请已审批，请勿重复操作"
            );
        }

        apply.setStatus(
                ClassroomApplyService.STATUS_FAIL
        );

        classroomApplyService.updateById(apply);

        return Result.success();
    }
}