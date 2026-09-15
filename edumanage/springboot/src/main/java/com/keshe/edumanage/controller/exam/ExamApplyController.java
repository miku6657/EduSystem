package com.keshe.edumanage.controller.exam;

import com.keshe.edumanage.common.result.Result;
import com.keshe.edumanage.entity.exam.ExamApply;
import com.keshe.edumanage.service.exam.ExamApplyService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 考核方式申报接口
 *
 * 教师申报
 *      ↓
 * 审核
 *      ↓
 * 导出申报结果
 */
@RestController
@RequestMapping("/api/exam-applies")
@RequiredArgsConstructor
public class ExamApplyController {

    private final ExamApplyService examApplyService;


    /**
     * 提交考核方式申报
     *
     * POST /api/exam-applies
     */
    @PostMapping
    public Result<Void> apply(
            @RequestBody ExamApply examApply
    ) {
        examApplyService.apply(examApply);
        return Result.success();
    }


    /**
     * 审核申报
     *
     * PUT /api/exam-applies/{id}/audit
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
        examApplyService.audit(id, status);
        return Result.success();
    }


    /**
     * 查询申报列表
     *
     * GET /api/exam-applies/export
     */
    @GetMapping("/export")
    public Result<List<ExamApply>> exportList(
            @RequestParam(required = false) String status
    ) {
        return Result.success(
                examApplyService.listForExport(status)
        );
    }


    /**
     * 查询申报详情
     *
     * GET /api/exam-applies/{id}
     */
    @GetMapping("/{id}")
    public Result<ExamApply> getById(
            @PathVariable Long id
    ) {
        return Result.success(
                examApplyService.getById(id)
        );
    }
}