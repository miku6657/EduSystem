package com.keshe.edumanage.service.attendance;

import com.baomidou.mybatisplus.extension.service.IService;
import com.keshe.edumanage.entity.attendance.TeacherAttendance;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 教师考勤业务接口
 * <p>教师签到、指纹考勤数据导入与出勤统计</p>
 */
public interface TeacherAttendanceService extends IService<TeacherAttendance> {

    /** 考勤状态：正常 */
    String STATUS_NORMAL = "正常";

    /**
     * 教师签到（当天重复签到时提示）
     *
     * @param teacherId 教师ID
     */
    void checkIn(Long teacherId);

    /**
     * 导入指纹考勤数据（当天已有记录时更新，供指纹考勤系统对接）
     *
     * @param teacherId      教师ID
     * @param attendanceDate 考勤日期
     * @param status         考勤状态
     * @param checkTime      签到时间
     */
    void importAttendance(Long teacherId, LocalDate attendanceDate, String status,
                          LocalDateTime checkTime);

    /**
     * 查询某天的教师考勤记录
     *
     * @param date 考勤日期
     * @return 考勤记录列表
     */
    List<TeacherAttendance> listByDate(LocalDate date);

    /**
     * 某天的教师出勤统计
     *
     * @param date 考勤日期
     * @return 统计结果：total（教师总数）、checked（已签到人数）、absent（未签到人数）
     */
    Map<String, Object> statByDate(LocalDate date);
}
