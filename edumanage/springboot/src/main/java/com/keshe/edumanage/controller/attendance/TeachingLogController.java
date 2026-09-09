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
 * <p>教师课后填写授课日志（最迟每周末提交），按周查询</p>
 */
@RestController
@RequestMapping("/api/teaching-log")
@RequiredArgsConstructor
public class TeachingLogController {

    private final TeachingLogService teachingLogService;

    /**
     * 教师新增授课日志（授课日期不允许晚于当天）
     */
    @PostMapping
    public Result<Void> add(@RequestBody TeachingLog teachingLog) {
        teachingLogService.addLog(teachingLog);
        return Result.success();
    }

    /**
     * 查询某位教师某周的授课日志（date 传该周任意一天）
     */
    @GetMapping("/list-by-teacher-week")
    public Result<List<TeachingLog>> listByTeacherWeek(
            @RequestParam Long teacherId,
            @RequestParam LocalDate date) {
        return Result.success(teachingLogService.listByTeacherWeek(teacherId, date));
    }

    /**
     * 查询某个班级某周的授课日志（date 传该周任意一天）
     */
    @GetMapping("/list-by-class-week")
    public Result<List<TeachingLog>> listByClassWeek(
            @RequestParam Long classId,
            @RequestParam LocalDate date) {
        return Result.success(teachingLogService.listByClassWeek(classId, date));
    }
}
