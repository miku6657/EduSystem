package com.keshe.edumanage.controller.exam;

import com.keshe.edumanage.common.result.Result;
import com.keshe.edumanage.entity.exam.ExamMonitor;
import com.keshe.edumanage.service.exam.ExamMonitorService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/exam-monitors")
@RequiredArgsConstructor
public class ExamMonitorController {

    private final ExamMonitorService examMonitorService;

    /**
     * 查询监考安排
     * GET /api/exam-monitors
     */
    @GetMapping
    public Result<List<ExamMonitor>> list(
            @RequestParam(required = false) Long examId,
            @RequestParam(required = false) Long teacherId
    ) {
        if (examId != null) {
            return Result.success(
                    examMonitorService.listByExam(examId)
            );
        }

        if (teacherId != null) {
            return Result.success(
                    examMonitorService.listByTeacher(teacherId)
            );
        }

        return Result.success(
                examMonitorService.list()
        );
    }

    /**
     * 查询监考详情
     * GET /api/exam-monitors/{id}
     */
    @GetMapping("/{id}")
    public Result<ExamMonitor> getById(
            @PathVariable Long id
    ) {
        return Result.success(
                examMonitorService.getById(id)
        );
    }

    /**
     * 新增监考安排
     * POST /api/exam-monitors
     */
    @PostMapping
    public Result<Void> save(
            @RequestBody ExamMonitor examMonitor
    ) {
        examMonitorService.save(examMonitor);
        return Result.success();
    }

    /**
     * 删除监考安排
     * DELETE /api/exam-monitors/{id}
     */
    @DeleteMapping("/{id}")
    public Result<Void> remove(
            @PathVariable Long id
    ) {
        examMonitorService.removeById(id);
        return Result.success();
    }
}
