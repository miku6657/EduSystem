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
@RequestMapping("/api/graduate-check")
@RequiredArgsConstructor
public class GraduateCheckController {

    private final GraduateCheckService graduateCheckService;

    /**
     * 保存审核结果（学分或课程未通过时，结论不允许为通过）
     */
    @PostMapping("/audit")
    public Result<Void> audit(@RequestBody GraduateCheck check) {
        graduateCheckService.audit(check);
        return Result.success();
    }

    /**
     * 按审核结论查询（checkStatus：WAIT/PASS/FAIL，为空查全部）
     */
    @GetMapping("/list")
    public Result<List<GraduateCheck>> list(@RequestParam(required = false) String checkStatus) {
        return Result.success(graduateCheckService.listByStatus(checkStatus));
    }

    /**
     * 查询某位学生的审核记录
     */
    @GetMapping("/by-student/{studentId}")
    public Result<GraduateCheck> getByStudent(@PathVariable Long studentId) {
        return Result.success(graduateCheckService.getByStudent(studentId));
    }
}
