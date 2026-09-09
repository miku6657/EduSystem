package com.keshe.edumanage.service.attendance;

import com.baomidou.mybatisplus.extension.service.IService;
import com.keshe.edumanage.entity.attendance.StudentAttendance;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * 学生考勤业务接口
 * <p>记录学生课程出勤，按周生成班级考勤报表</p>
 */
public interface StudentAttendanceService extends IService<StudentAttendance> {

    /** 考勤状态：正常 */
    String STATUS_NORMAL = "正常";

    /**
     * 批量录入学生考勤（同一学生同一课程同一天已有记录时覆盖更新）
     *
     * @param records 考勤记录列表（学生ID、课程ID、考勤日期、状态）
     */
    void recordBatch(List<StudentAttendance> records);

    /**
     * 查询某位学生在日期区间内的考勤记录
     *
     * @param studentId 学生ID
     * @param startDate 开始日期
     * @param endDate   结束日期
     * @return 考勤记录列表
     */
    List<StudentAttendance> listByStudent(Long studentId, LocalDate startDate, LocalDate endDate);

    /**
     * 班级学生周考勤报表
     *
     * @param classId      班级ID
     * @param anyDayOfWeek 该周内任意一天
     * @return 报表数据：每行包含 studentNo、name、total（记录总数）、
     *         normal（正常次数）、absent（缺勤次数）
     */
    List<Map<String, Object>> weeklyReport(Long classId, LocalDate anyDayOfWeek);
}
