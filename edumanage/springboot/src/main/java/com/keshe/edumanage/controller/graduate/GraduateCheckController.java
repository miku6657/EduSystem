package com.keshe.edumanage.controller.graduate;

import com.keshe.edumanage.common.result.Result;
import com.keshe.edumanage.entity.graduate.GraduateCheck;
import com.keshe.edumanage.service.graduate.GraduateCheckService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 毕业资格审核接口
 */
@RestController
@RequestMapping("/api/graduate-checks")
@RequiredArgsConstructor
public class GraduateCheckController {

    private final GraduateCheckService graduateCheckService;


    /**
     * 保存毕业资格审核结果
     *
     * POST /api/graduate-checks/audit
     */
    @PostMapping("/audit")
    public Result<Void> audit(
            @RequestBody GraduateCheck check
    ) {
        graduateCheckService.audit(check);
        return Result.success();
    }


    /**
     * 查询审核记录
     *
     * GET /api/graduate-checks
     *
     * checkStatus:
     * WAIT
     * PASS
     * FAIL
     */
    @GetMapping
    public Result<List<GraduateCheck>> list(
            @RequestParam(required = false) String checkStatus
    ) {
        return Result.success(
                graduateCheckService.listByStatus(checkStatus)
        );
    }


    /**
     * 查询学生毕业审核记录
     *
     * GET /api/graduate-checks/students/{studentId}
     */
    @GetMapping("/students/{studentId}")
    public Result<GraduateCheck> getByStudent(
            @PathVariable Long studentId
    ) {
        return Result.success(
                graduateCheckService.getByStudent(studentId)
        );
    }
}
