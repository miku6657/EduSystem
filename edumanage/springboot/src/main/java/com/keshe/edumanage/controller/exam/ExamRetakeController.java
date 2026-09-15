package com.keshe.edumanage.controller.exam;

import com.keshe.edumanage.common.result.Result;
import com.keshe.edumanage.entity.exam.ExamRetake;
import com.keshe.edumanage.service.exam.ExamRetakeService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 补考重修接口
 *
 * 学生申请补考/重修
 * 教务安排考试
 */
@RestController
@RequestMapping("/api/exam-retakes")
@RequiredArgsConstructor
public class ExamRetakeController {

    private final ExamRetakeService examRetakeService;


    /**
     * 学生申请补考/重修
     *
     * POST /api/exam-retakes
     */
    @PostMapping
    public Result<Void> apply(
            @RequestParam Long studentId,
            @RequestParam Long courseId
    ) {
        examRetakeService.applyRetake(
                studentId,
                courseId
        );

        return Result.success();
    }


    /**
     * 安排补考/重修考试
     *
     * PUT /api/exam-retakes/{id}/assign
     */
    @PutMapping("/{id}/assign")
    public Result<Void> assign(
            @PathVariable Long id,
            @RequestParam Long examId
    ) {
        examRetakeService.assignExam(
                id,
                examId
        );

        return Result.success();
    }


    /**
     * 查询学生补考重修记录
     *
     * GET /api/exam-retakes/students/{studentId}
     */
    @GetMapping("/students/{studentId}")
    public Result<List<ExamRetake>> listByStudent(
            @PathVariable Long studentId
    ) {
        return Result.success(
                examRetakeService.listByStudent(studentId)
        );
    }


    /**
     * 按类型查询
     *
     * GET /api/exam-retakes/type/{type}
     *
     * type:
     * 补考
     * 重修
     */
    @GetMapping("/type/{type}")
    public Result<List<ExamRetake>> listByType(
            @PathVariable String type
    ) {
        return Result.success(
                examRetakeService.listByType(type)
        );
    }
}
