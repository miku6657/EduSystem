package com.keshe.edumanage.controller.exam;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.keshe.edumanage.common.result.Result;
import com.keshe.edumanage.entity.exam.ExamScore;
import com.keshe.edumanage.service.exam.ExamScoreService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/exam-scores")
@RequiredArgsConstructor
public class ExamScoreController {

    private final ExamScoreService examScoreService;

    /**
     * 批量录入某场考试成绩
     * POST /api/exam-scores/exams/{examId}
     */
    @PostMapping("/exams/{examId}")
    public Result<Void> save(
            @PathVariable Long examId,
            @RequestBody List<ExamScore> scores
    ) {
        examScoreService.saveScores(examId, scores);
        return Result.success();
    }

    /**
     * 查询考试成绩
     * GET /api/exam-scores?examId=xxx
     */
    @GetMapping
    public Result<Page<ExamScore>> page(
            @RequestParam(defaultValue = "1") Integer pageNo,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam Long examId,
            @RequestParam(required = false) String status
    ) {
        Page<ExamScore> page = examScoreService.pageByExam(
                new Page<>(pageNo, pageSize),
                examId,
                status
        );
        return Result.success(page);
    }

    /**
     * 查询学生全部成绩
     * GET /api/exam-scores/students/{studentId}
     */
    @GetMapping("/students/{studentId}")
    public Result<List<ExamScore>> listByStudent(
            @PathVariable Long studentId
    ) {
        return Result.success(
                examScoreService.listByStudent(studentId)
        );
    }

    /**
     * 考试成绩统计
     * GET /api/exam-scores/exams/{examId}/statistics
     */
    @GetMapping("/exams/{examId}/statistics")
    public Result<Map<String, Object>> stat(
            @PathVariable Long examId
    ) {
        return Result.success(
                examScoreService.statByExam(examId)
        );
    }
}
