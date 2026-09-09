package com.keshe.edumanage.controller.exam;

import com.keshe.edumanage.common.result.Result;
import com.keshe.edumanage.entity.exam.ExamRetake;
import com.keshe.edumanage.service.exam.ExamRetakeService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 补考重修接口
 * <p>学生网上申请重修、教务安排补考/重修场次</p>
 */
@RestController
@RequestMapping("/api/retake")
@RequiredArgsConstructor
public class ExamRetakeController {

    private final ExamRetakeService examRetakeService;

    /**
     * 学生申请重修
     */
    @PostMapping("/apply")
    public Result<Void> apply(@RequestParam Long studentId, @RequestParam Long courseId) {
        examRetakeService.applyRetake(studentId, courseId);
        return Result.success();
    }

    /**
     * 安排补考/重修考试场次
     */
    @PutMapping("/assign/{id}")
    public Result<Void> assign(@PathVariable Long id, @RequestParam Long examId) {
        examRetakeService.assignExam(id, examId);
        return Result.success();
    }

    /**
     * 查询某位学生的补考重修记录
     */
    @GetMapping("/list-by-student/{studentId}")
    public Result<List<ExamRetake>> listByStudent(@PathVariable Long studentId) {
        return Result.success(examRetakeService.listByStudent(studentId));
    }

    /**
     * 按类型查询（type：补考 / 重修）
     */
    @GetMapping("/list-by-type/{type}")
    public Result<List<ExamRetake>> listByType(@PathVariable String type) {
        return Result.success(examRetakeService.listByType(type));
    }
}
