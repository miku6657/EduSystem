package com.keshe.edumanage.controller.base;

import com.keshe.edumanage.common.result.Result;
import com.keshe.edumanage.entity.base.CourseAdjust;
import com.keshe.edumanage.entity.base.Teacher;
import com.keshe.edumanage.service.base.CourseAdjustService;
import com.keshe.edumanage.service.base.TeacherService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 调课申请接口
 * <p>教师端：提交 / 我的调课 / 撤销；管理端：审批列表 / 通过 / 驳回。</p>
 */
@RestController
@RequestMapping("/api/course-adjusts")
@RequiredArgsConstructor
public class CourseAdjustController {

    private final CourseAdjustService courseAdjustService;
    private final TeacherService teacherService;

    /**
     * 教师提交调课申请
     * POST /api/course-adjusts
     */
    @PostMapping
    public Result<Void> submit(
            @RequestBody CourseAdjust adjust,
            Authentication authentication
    ) {
        Teacher teacher = currentTeacher(authentication);
        if (teacher == null) {
            return Result.fail("只有教师可以提交调课申请");
        }
        adjust.setTeacherId(teacher.getId());
        courseAdjustService.submit(adjust);
        return Result.success();
    }

    /**
     * 我的调课（当前登录教师）
     * GET /api/course-adjusts/my
     */
    @GetMapping("/my")
    public Result<List<CourseAdjust>> myList(
            Authentication authentication
    ) {
        Teacher teacher = currentTeacher(authentication);
        if (teacher == null) {
            return Result.success(List.of());
        }
        return Result.success(
                courseAdjustService.listByTeacher(teacher.getId())
        );
    }

    /**
     * 调课申请详情
     * GET /api/course-adjusts/{id}
     */
    @GetMapping("/{id}")
    public Result<CourseAdjust> getById(
            @PathVariable Long id
    ) {
        return Result.success(
                courseAdjustService.getById(id)
        );
    }

    /**
     * 审批列表（管理端，status 为空查全部）
     * GET /api/course-adjusts?status=待审核
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Result<List<CourseAdjust>> list(
            @RequestParam(required = false) String status
    ) {
        return Result.success(
                courseAdjustService.listByStatus(status)
        );
    }

    /**
     * 审批通过（管理端）
     * PUT /api/course-adjusts/{id}/approve
     */
    @PutMapping("/{id}/approve")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> approve(
            @PathVariable Long id,
            @RequestParam(required = false) String remark
    ) {
        courseAdjustService.approve(id, remark);
        return Result.success();
    }

    /**
     * 审批驳回（管理端）
     * PUT /api/course-adjusts/{id}/reject
     */
    @PutMapping("/{id}/reject")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> reject(
            @PathVariable Long id,
            @RequestParam(required = false) String remark
    ) {
        courseAdjustService.reject(id, remark);
        return Result.success();
    }

    /**
     * 教师撤销自己的申请（仅待审核可撤销）
     * PUT /api/course-adjusts/{id}/cancel
     */
    @PutMapping("/{id}/cancel")
    public Result<Void> cancel(
            @PathVariable Long id,
            Authentication authentication
    ) {
        Teacher teacher = currentTeacher(authentication);
        if (teacher == null) {
            return Result.fail("只有教师可以撤销调课申请");
        }
        courseAdjustService.cancel(id, teacher.getId());
        return Result.success();
    }

    /** 当前登录人 → 教师（登录名即工号） */
    private Teacher currentTeacher(Authentication authentication) {
        if (authentication == null || authentication.getName() == null) {
            return null;
        }
        return teacherService.getByTeacherNo(authentication.getName());
    }
}
