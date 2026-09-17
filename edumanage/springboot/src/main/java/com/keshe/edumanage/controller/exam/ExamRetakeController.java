package com.keshe.edumanage.controller.exam;

import com.keshe.edumanage.common.result.Result;
import com.keshe.edumanage.dto.ExamArrangeDTO;
import com.keshe.edumanage.entity.exam.ExamRetake;
import com.keshe.edumanage.entity.exam.ExamScore;
import com.keshe.edumanage.service.exam.ExamRetakeService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(
        "/api/exam-retakes"
)
@RequiredArgsConstructor
public class ExamRetakeController {

    private final ExamRetakeService
            examRetakeService;

    /**
     * 学生申请重修。
     */
    @PostMapping
    public Result<Void> apply(
            @RequestParam Long studentId,
            @RequestParam Long courseId
    ) {

        examRetakeService
                .applyRetake(
                        studentId,
                        courseId
                );

        return Result.success();
    }

    /**
     * admin批准。
     */
    @PutMapping("/{id}/approve")
    public Result<Void> approve(
            @PathVariable Long id
    ) {

        examRetakeService
                .approve(id);

        return Result.success();
    }

    /**
     * admin驳回。
     */
    @PutMapping("/{id}/reject")
    public Result<Void> reject(
            @PathVariable Long id
    ) {

        examRetakeService
                .reject(id);

        return Result.success();
    }

    /**
     * admin安排考试。
     */
    @PutMapping("/{id}/assign")
    public Result<Void> assign(
            @PathVariable Long id,
            @RequestParam Long examId
    ) {

        examRetakeService
                .assignExam(
                        id,
                        examId
                );

        return Result.success();
    }

    /**
     * admin：
     * 为审批通过的重修申请
     * 创建一场专门的重修考试。
     */
    @PostMapping("/{id}/arrange")
    public Result<Void> arrange(
            @PathVariable Long id,
            @RequestBody ExamArrangeDTO dto
    ) {

        examRetakeService
            .arrangeRetakeExam(
                id,
                dto
            );

        return Result.success();
    }

    /**
     * 学生自己的申请。
     */
    @GetMapping(
            "/students/{studentId}"
    )
    public Result<List<ExamRetake>>
    listByStudent(
            @PathVariable Long studentId
    ) {

        return Result.success(
                examRetakeService
                        .listByStudent(
                                studentId
                        )
        );
    }

    /**
     * admin查询补考/重修。
     */
    @GetMapping("/type/{type}")
    public Result<List<ExamRetake>>
    listByType(
            @PathVariable String type
    ) {

        return Result.success(
                examRetakeService
                        .listByType(type)
        );
    }

    /**
     * 教师查询某场重修考试学生。
     */
    @GetMapping(
            "/exams/{examId}"
    )
    public Result<List<ExamRetake>>
    listByExam(
            @PathVariable Long examId
    ) {

        return Result.success(
                examRetakeService
                        .listByExam(examId)
        );
    }

    /**
     * 教师录入重修成绩。
     *
     * 会覆盖原成绩。
     */
    @PostMapping(
            "/exams/{examId}/scores"
    )
    public Result<Void>
    saveRetakeScores(
            @PathVariable Long examId,
            @RequestBody
            List<ExamScore> scores
    ) {

        examRetakeService
                .saveRetakeScores(
                        examId,
                        scores
                );

        return Result.success();
    }
}
