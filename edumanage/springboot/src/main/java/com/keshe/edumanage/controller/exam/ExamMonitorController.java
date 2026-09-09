package com.keshe.edumanage.controller.exam;

import com.keshe.edumanage.common.result.Result;
import com.keshe.edumanage.entity.exam.ExamMonitor;
import com.keshe.edumanage.service.exam.ExamMonitorService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 监考安排接口
 */
@RestController
@RequestMapping("/api/exam-monitor")
@RequiredArgsConstructor
public class ExamMonitorController {

    private final ExamMonitorService examMonitorService;

    /**
     * 查询某场考试的监考安排
     */
    @GetMapping("/list-by-exam/{examId}")
    public Result<List<ExamMonitor>> listByExam(@PathVariable Long examId) {
        return Result.success(examMonitorService.listByExam(examId));
    }

    /**
     * 查询某位教师的监考任务
     */
    @GetMapping("/list-by-teacher/{teacherId}")
    public Result<List<ExamMonitor>> listByTeacher(@PathVariable Long teacherId) {
        return Result.success(examMonitorService.listByTeacher(teacherId));
    }
}
