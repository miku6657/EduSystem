package com.keshe.edumanage.controller.exam;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.keshe.edumanage.common.result.Result;
import com.keshe.edumanage.entity.exam.ExamScore;
import com.keshe.edumanage.service.exam.ExamScoreService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 考试成绩接口
 * <p>成绩录入、查询与考试数据统计（应考/实考/缺考等）</p>
 */
@RestController
@RequestMapping("/api/score")
@RequiredArgsConstructor
public class ExamScoreController {

    private final ExamScoreService examScoreService;

    /**
     * 批量录入某场考试的成绩（已有记录时更新）
     */
    @PostMapping("/save/{examId}")
    public Result<Void> save(@PathVariable Long examId,
                             @RequestBody List<ExamScore> scores) {
        examScoreService.saveScores(examId, scores);
        return Result.success();
    }

    /**
     * 分页查询某场考试的成绩
     */
    @GetMapping("/page-by-exam")
    public Result<Page<ExamScore>> pageByExam(
            @RequestParam(defaultValue = "1") Integer pageNo,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam Long examId,
            @RequestParam(required = false) String status) {
        Page<ExamScore> page = examScoreService.pageByExam(
                new Page<>(pageNo, pageSize), examId, status);
        return Result.success(page);
    }

    /**
     * 查询某位学生的全部成绩
     */
    @GetMapping("/list-by-student/{studentId}")
    public Result<List<ExamScore>> listByStudent(@PathVariable Long studentId) {
        return Result.success(examScoreService.listByStudent(studentId));
    }

    /**
     * 考试数据统计（应考人数、实考人数、缺考人数、及格/不及格人数）
     */
    @GetMapping("/stat/{examId}")
    public Result<Map<String, Object>> stat(@PathVariable Long examId) {
        return Result.success(examScoreService.statByExam(examId));
    }
}
