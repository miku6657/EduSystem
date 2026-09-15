package com.keshe.edumanage.controller.attendance;

import com.keshe.edumanage.common.result.Result;
import com.keshe.edumanage.entity.attendance.TeachingLog;
import com.keshe.edumanage.service.attendance.TeachingLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * 教学日志接口
 *
 * 教师课后填写授课日志
 * 按周查询教学记录
 */
@RestController
@RequestMapping("/api/teaching-logs")
@RequiredArgsConstructor
public class TeachingLogController {

    private final TeachingLogService teachingLogService;


    /**
     * 新增教学日志
     *
     * POST /api/teaching-logs
     */
    @PostMapping
    public Result<Void> add(
            @RequestBody TeachingLog teachingLog
    ) {
        teachingLogService.addLog(teachingLog);
        return Result.success();
    }


    /**
     * 查询教师某周教学日志
     *
     * GET /api/teaching-logs/teachers/{teacherId}/weekly
     */
    @GetMapping("/teachers/{teacherId}/weekly")
    public Result<List<TeachingLog>> listByTeacherWeek(
            @PathVariable Long teacherId,
            @RequestParam LocalDate date
    ) {
        return Result.success(
                teachingLogService.listByTeacherWeek(
                        teacherId,
                        date
                )
        );
    }


    /**
     * 查询班级某周教学日志
     *
     * GET /api/teaching-logs/classes/{classId}/weekly
     */
    @GetMapping("/classes/{classId}/weekly")
    public Result<List<TeachingLog>> listByClassWeek(
            @PathVariable Long classId,
            @RequestParam LocalDate date
    ) {
        return Result.success(
                teachingLogService.listByClassWeek(
                        classId,
                        date
                )
        );
    }
}