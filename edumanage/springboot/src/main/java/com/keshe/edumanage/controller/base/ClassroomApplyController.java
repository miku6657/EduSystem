package com.keshe.edumanage.controller.base;

import com.keshe.edumanage.common.result.Result;
import com.keshe.edumanage.entity.base.ClassroomApply;
import com.keshe.edumanage.service.base.ClassroomApplyService;
import com.keshe.edumanage.vo.ClassroomApplyVO;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@RestController
@RequestMapping("/api/classroom-applies")
@RequiredArgsConstructor
public class ClassroomApplyController {

    private final ClassroomApplyService classroomApplyService;

    /**
     * ==============================
     * 师生端：提交教室申请
     * ==============================
     *
     * POST /api/classroom-applies
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('STUDENT', 'TEACHER')")
    public Result<Void> apply(
            @RequestBody ClassroomApply apply,
            Authentication authentication
    ) {

        if (authentication == null) {
            return Result.error(
                    401,
                    "未登录"
            );
        }

        /**
         * 基本参数检查
         */
        if (apply.getRoomId() == null) {
            return Result.fail(
                    "请选择教室"
            );
        }

        if (apply.getApplyDate() == null) {
            return Result.fail(
                    "请选择使用日期"
            );
        }

        if (
                apply.getApplyDate()
                        .isBefore(LocalDate.now())
        ) {
            return Result.fail(
                    "使用日期不能早于今天"
            );
        }

        if (
                apply.getTimeSlot() == null
                        || apply.getTimeSlot().isBlank()
        ) {
            return Result.fail(
                    "请选择使用时段"
            );
        }

        if (
                apply.getReason() == null
                        || apply.getReason().isBlank()
        ) {
            return Result.fail(
                    "请填写申请理由"
            );
        }

        /**
         * 冲突检测：
         *
         * 同教室
         * + 同日期
         * + 同时段
         *
         * 已有：
         * 待审核 / 已通过
         *
         * 都不能再次申请。
         */
        boolean conflict =
                classroomApplyService
                        .checkConflict(
                                apply.getRoomId(),
                                apply.getApplyDate(),
                                apply.getTimeSlot()
                        );

        if (conflict) {
            return Result.fail(
                    "该教室在该时间段已有申请或已被占用"
            );
        }

        /**
         * 申请人绝对不能相信前端传值。
         *
         * 直接读取 JWT 当前用户。
         */
        apply.setApplicant(
                authentication.getName()
        );

        /**
         * 新申请状态统一：
         * 待审核
         */
        apply.setStatus(
                "待审核"
        );

        apply.setCreateTime(
                LocalDateTime.now()
        );

        classroomApplyService.save(
                apply
        );

        return Result.success();
    }

    /**
     * ==============================
     * 师生端：我的申请
     * ==============================
     *
     * GET /api/classroom-applies/my
     */
    @GetMapping("/my")
    @PreAuthorize("hasAnyRole('STUDENT', 'TEACHER')")
    public Result<List<ClassroomApplyVO>> myList(
            Authentication authentication
    ) {

        if (authentication == null) {
            return Result.error(
                    401,
                    "未登录"
            );
        }

        return Result.success(
                classroomApplyService
                        .listByApplicantVO(
                                authentication.getName()
                        )
        );
    }

    /**
     * ==============================
     * 师生端：撤回申请
     * ==============================
     *
     * 只有：
     *
     * 本人的申请
     * +
     * 待审核
     *
     * 才允许撤回。
     *
     * PUT
     * /api/classroom-applies/{id}/cancel
     */
    @PutMapping("/{id}/cancel")
    @PreAuthorize("hasAnyRole('STUDENT', 'TEACHER')")
    public Result<Void> cancel(
            @PathVariable Long id,
            Authentication authentication
    ) {

        if (authentication == null) {
            return Result.error(
                    401,
                    "未登录"
            );
        }

        ClassroomApply apply =
                classroomApplyService
                        .getById(id);

        if (apply == null) {
            return Result.fail(
                    "申请记录不存在"
            );
        }

        /**
         * 只能撤回自己的申请。
         */
        if (
                !Objects.equals(
                        apply.getApplicant(),
                        authentication.getName()
                )
        ) {
            return Result.error(
                    403,
                    "无权撤回该申请"
            );
        }

        /**
         * 已经审批过的不能撤回。
         */
        if (
                !"待审核".equals(
                        apply.getStatus()
                )
        ) {
            return Result.fail(
                    "当前申请已处理，不能撤回"
            );
        }

        apply.setStatus(
                "已取消"
        );

        classroomApplyService
                .updateById(apply);

        return Result.success();
    }

    /**
     * ==============================
     * ADMIN：审批列表
     * ==============================
     *
     * GET
     * /api/classroom-applies
     *
     * GET
     * /api/classroom-applies?status=待审核
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Result<List<ClassroomApplyVO>> list(
            @RequestParam(required = false)
            String status
    ) {

        return Result.success(
                classroomApplyService
                        .listVO(status)
        );
    }

    /**
     * ==============================
     * ADMIN：通过
     * ==============================
     *
     * PUT
     * /api/classroom-applies/{id}/approve
     */
    @PutMapping("/{id}/approve")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> approve(
            @PathVariable Long id
    ) {

        ClassroomApply apply =
                classroomApplyService
                        .getById(id);

        if (apply == null) {
            return Result.fail(
                    "申请记录不存在"
            );
        }

        /**
         * 防止：
         *
         * 已通过再次通过
         * 已驳回再次通过
         * 已取消再通过
         */
        if (
                !"待审核".equals(
                        apply.getStatus()
                )
        ) {
            return Result.fail(
                    "该申请已经处理，不能重复审批"
            );
        }

        apply.setStatus(
                "已通过"
        );

        classroomApplyService
                .updateById(apply);

        return Result.success();
    }

    /**
     * ==============================
     * ADMIN：驳回
     * ==============================
     *
     * PUT
     * /api/classroom-applies/{id}/reject
     */
    @PutMapping("/{id}/reject")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> reject(
            @PathVariable Long id
    ) {

        ClassroomApply apply =
                classroomApplyService
                        .getById(id);

        if (apply == null) {
            return Result.fail(
                    "申请记录不存在"
            );
        }

        if (
                !"待审核".equals(
                        apply.getStatus()
                )
        ) {
            return Result.fail(
                    "该申请已经处理，不能重复审批"
            );
        }

        apply.setStatus(
                "已驳回"
        );

        classroomApplyService
                .updateById(apply);

        return Result.success();
    }
}
