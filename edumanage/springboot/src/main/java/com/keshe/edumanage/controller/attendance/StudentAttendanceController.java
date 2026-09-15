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
 *
 * 批量点名、出勤查询、班级周考勤报表
 */
@RestController
@RequestMapping("/api/student-attendances")
@RequiredArgsConstructor
public class StudentAttendanceController {

    private final StudentAttendanceService studentAttendanceService;


    /**
     * 批量录入学生考勤
     *
     * POST /api/student-attendances
     */
    @PostMapping
    public Result<Void> record(
            @RequestBody List<StudentAttendance> records
    ) {
        studentAttendanceService.recordBatch(records);
        return Result.success();
    }


    /**
     * 查询学生考勤记录
     *
     * GET /api/student-attendances/students/{studentId}
     */
    @GetMapping("/students/{studentId}")
    public Result<List<StudentAttendance>> listByStudent(
            @PathVariable Long studentId,
            @RequestParam LocalDate startDate,
            @RequestParam LocalDate endDate
    ) {
        return Result.success(
                studentAttendanceService.listByStudent(
                        studentId,
                        startDate,
                        endDate
                )
        );
    }


    /**
     * 班级周考勤报表
     *
     * GET /api/student-attendances/weekly-report
     */
    @GetMapping("/weekly-report")
    public Result<List<Map<String, Object>>> weeklyReport(
            @RequestParam Long classId,
            @RequestParam LocalDate date
    ) {
        return Result.success(
                studentAttendanceService.weeklyReport(
                        classId,
                        date
                )
        );
    }
}
