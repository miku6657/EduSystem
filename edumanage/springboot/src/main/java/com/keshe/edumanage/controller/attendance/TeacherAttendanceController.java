package com.keshe.edumanage.controller.attendance;

import com.keshe.edumanage.common.result.Result;
import com.keshe.edumanage.entity.attendance.TeacherAttendance;
import com.keshe.edumanage.service.attendance.TeacherAttendanceService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 教师考勤接口
 *
 * 教师签到、指纹考勤数据导入、出勤统计
 */
@RestController
@RequestMapping("/api/teacher-attendances")
@RequiredArgsConstructor
public class TeacherAttendanceController {

    private final TeacherAttendanceService teacherAttendanceService;


    /**
     * 教师签到
     *
     * POST /api/teacher-attendances/{teacherId}/check-in
     */
    @PostMapping("/{teacherId}/check-in")
    public Result<Void> checkIn(
            @PathVariable Long teacherId
    ) {
        teacherAttendanceService.checkIn(teacherId);
        return Result.success();
    }


    /**
     * 导入指纹考勤数据
     *
     * POST /api/teacher-attendances/import
     */
    @PostMapping("/import")
    public Result<Void> importAttendance(
            @RequestParam Long teacherId,
            @RequestParam LocalDate attendanceDate,
            @RequestParam String status,
            @RequestParam LocalDateTime checkTime
    ) {
        teacherAttendanceService.importAttendance(
                teacherId,
                attendanceDate,
                status,
                checkTime
        );

        return Result.success();
    }


    /**
     * 查询某天教师考勤记录
     *
     * GET /api/teacher-attendances?date=2026-09-15
     */
    @GetMapping
    public Result<List<TeacherAttendance>> listByDate(
            @RequestParam LocalDate date
    ) {
        return Result.success(
                teacherAttendanceService.listByDate(date)
        );
    }


    /**
     * 教师出勤统计
     *
     * GET /api/teacher-attendances/statistics?date=2026-09-15
     */
    @GetMapping("/statistics")
    public Result<Map<String, Object>> statByDate(
            @RequestParam LocalDate date
    ) {
        return Result.success(
                teacherAttendanceService.statByDate(date)
        );
    }
}