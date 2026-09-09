package com.keshe.edumanage.controller.attendance;

import com.keshe.edumanage.common.result.Result;
import com.keshe.edumanage.entity.attendance.StudentAttendance;
import com.keshe.edumanage.service.attendance.StudentAttendanceService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * 学生考勤接口
 * <p>批量点名、出勤查询、班级周考勤报表</p>
 */
@RestController
@RequestMapping("/api/student-attendance")
@RequiredArgsConstructor
public class StudentAttendanceController {

    private final StudentAttendanceService studentAttendanceService;

    /**
     * 批量录入学生考勤（同学生同课程同日期已有记录时覆盖更新）
     */
    @PostMapping("/record")
    public Result<Void> record(@RequestBody List<StudentAttendance> records) {
        studentAttendanceService.recordBatch(records);
        return Result.success();
    }

    /**
     * 查询某位学生在日期区间内的考勤记录
     */
    @GetMapping("/list-by-student")
    public Result<List<StudentAttendance>> listByStudent(
            @RequestParam Long studentId,
            @RequestParam LocalDate startDate,
            @RequestParam LocalDate endDate) {
        return Result.success(
                studentAttendanceService.listByStudent(studentId, startDate, endDate));
    }

    /**
     * 班级学生周考勤报表（date 传该周任意一天）
     */
    @GetMapping("/weekly-report")
    public Result<List<Map<String, Object>>> weeklyReport(
            @RequestParam Long classId,
            @RequestParam LocalDate date) {
        return Result.success(studentAttendanceService.weeklyReport(classId, date));
    }
}
