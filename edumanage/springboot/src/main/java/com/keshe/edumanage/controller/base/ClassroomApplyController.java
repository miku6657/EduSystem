package com.keshe.edumanage.controller.base;


import com.keshe.edumanage.common.result.Result;
import com.keshe.edumanage.entity.base.ClassroomApply;
import com.keshe.edumanage.service.base.ClassroomApplyService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;
import java.util.List;


@RestController
@RequiredArgsConstructor
public class ClassroomApplyController {


    private final ClassroomApplyService classroomApplyService;

    @PostMapping("/api/classroom/apply")
    public Result<Void> apply(
            @RequestBody ClassroomApply apply
    ){
        boolean conflict =
                classroomApplyService.checkConflict(
                        apply.getRoomId(),
                        apply.getApplyDate(),
                        apply.getTimeSlot()
                );
        if(conflict){
            return Result.fail(
                    "该时间段教室已被占用"
            );
        }
        apply.setStatus(
                "待审核"
        );
        classroomApplyService.save(
                apply
        );
        return Result.success();
    }
    @GetMapping("/api/classroom-apply/my/list")
    public Result<List<ClassroomApply>> myList(
            Authentication authentication
    ){
        String username =
                authentication.getName();
        return Result.success(
                classroomApplyService.listByApplicant(
                        username
                )
        );
    }

    @GetMapping("/api/classroom/approval/list")
    public Result<List<ClassroomApply>> approvalList(
            @RequestParam(required = false)
            String status
    ){

        return Result.success(
                classroomApplyService.list()
        );

    }

    @PutMapping("/api/classroom/approve/{id}")
    public Result<Void> approve(
            @PathVariable Long id
    ){

        ClassroomApply apply =
                classroomApplyService.getById(id);


        apply.setStatus("已通过");


        classroomApplyService.updateById(apply);


        return Result.success();

    }

    @PutMapping("/api/classroom/reject/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> reject(
            @PathVariable Long id
    ){
        ClassroomApply apply =
                classroomApplyService.getById(id);
        apply.setStatus("已驳回");
        classroomApplyService.updateById(apply);
        return Result.success();

    }

}