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
 * <p>教师签到、指纹考勤数据导入、出勤统计</p>
 */
@RestController
@RequestMapping("/api/teacher-attendance")
@RequiredArgsConstructor
public class TeacherAttendanceController {

    private final TeacherAttendanceService teacherAttendanceService;

    /**
     * 教师签到（一天一次）
     */
    @PostMapping("/check-in/{teacherId}")
    public Result<Void> checkIn(@PathVariable Long teacherId) {
        teacherAttendanceService.checkIn(teacherId);
        return Result.success();
    }

    /**
     * 导入指纹考勤数据
     */
    @PostMapping("/import")
    public Result<Void> importAttendance(@RequestParam Long teacherId,
                                         @RequestParam LocalDate attendanceDate,
                                         @RequestParam String status,
                                         @RequestParam LocalDateTime checkTime) {
        teacherAttendanceService.importAttendance(teacherId, attendanceDate, status, checkTime);
        return Result.success();
    }

    /**
     * 查询某天的教师考勤记录
     */
    @GetMapping("/list-by-date")
    public Result<List<TeacherAttendance>> listByDate(@RequestParam LocalDate date) {
        return Result.success(teacherAttendanceService.listByDate(date));
    }

    /**
     * 某天的教师出勤统计（教师总数、已签到人数、未签到人数）
     */
    @GetMapping("/stat-by-date")
    public Result<Map<String, Object>> statByDate(@RequestParam LocalDate date) {
        return Result.success(teacherAttendanceService.statByDate(date));
    }
}
