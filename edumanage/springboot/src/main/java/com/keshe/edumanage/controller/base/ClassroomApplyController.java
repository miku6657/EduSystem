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
            @RequestBody ClassroomApply apply
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

        apply.setStatus("待审核");

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
     * 审批列表
     */
    @GetMapping
    public Result<List<ClassroomApply>> list(
            @RequestParam(required = false) String status
    ) {
        return Result.success(
                classroomApplyService.list()
        );
    }

    /**
     * 通过申请
     */
    @PutMapping("/{id}/approve")
    public Result<Void> approve(
            @PathVariable Long id
    ) {
        ClassroomApply apply =
                classroomApplyService.getById(id);

        apply.setStatus("已通过");

        classroomApplyService.updateById(apply);

        return Result.success();
    }

    /**
     * 驳回申请
     */
    @PutMapping("/{id}/reject")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> reject(
            @PathVariable Long id
    ) {
        ClassroomApply apply =
                classroomApplyService.getById(id);

        apply.setStatus("已驳回");

        classroomApplyService.updateById(apply);

        return Result.success();
    }
}