package com.keshe.edumanage.controller.exam;

import com.keshe.edumanage.common.result.Result;
import com.keshe.edumanage.entity.exam.ExamApply;
import com.keshe.edumanage.service.exam.ExamApplyService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 考核方式申报接口
 * <p>教师申报 → 教研室/系主任审核 → 教务处审批；支持导出考核方式总表</p>
 */
@RestController
@RequestMapping("/api/exam-apply")
@RequiredArgsConstructor
public class ExamApplyController {

    private final ExamApplyService examApplyService;

    /**
     * 教师提交考核方式申报
     */
    @PostMapping("/apply")
    public Result<Void> apply(@RequestBody ExamApply examApply) {
        examApplyService.apply(examApply);
        return Result.success();
    }

    /**
     * 审核申报（status：PASS 通过 / FAIL 驳回）
     */
    @PutMapping("/audit/{id}")
    public Result<Void> audit(@PathVariable Long id, @RequestParam String status) {
        examApplyService.audit(id, status);
        return Result.success();
    }

    /**
     * 查询申报列表（status 为空查全部，用于导出考核方式总表）
     */
    @GetMapping("/export-list")
    public Result<List<ExamApply>> exportList(@RequestParam(required = false) String status) {
        return Result.success(examApplyService.listForExport(status));
    }

    /**
     * 根据ID查询申报详情
     */
    @GetMapping("/{id}")
    public Result<ExamApply> getById(@PathVariable Long id) {
        return Result.success(examApplyService.getById(id));
    }
}
