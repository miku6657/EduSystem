package com.keshe.edumanage.service.attendance;

import com.baomidou.mybatisplus.extension.service.IService;
import com.keshe.edumanage.entity.attendance.TeachingLog;

import java.time.LocalDate;
import java.util.List;

/**
 * 教学日志业务接口
 * <p>教师课后填写授课日志（授课内容及学生出勤情况），最迟于每周末提交</p>
 */
public interface TeachingLogService extends IService<TeachingLog> {

    /**
     * 新增授课日志（授课日期不允许晚于当天，保证日志课后填写）
     *
     * @param teachingLog 日志信息（教师、课程、班级、授课日期、授课内容、作业）
     */
    void addLog(TeachingLog teachingLog);

    /**
     * 查询某位教师某一周的授课日志
     *
     * @param teacherId     教师ID
     * @param anyDayOfWeek  该周内任意一天
     * @return 日志列表
     */
    List<TeachingLog> listByTeacherWeek(Long teacherId, LocalDate anyDayOfWeek);

    /**
     * 查询某个班级某一周的授课日志
     *
     * @param classId       班级ID
     * @param anyDayOfWeek  该周内任意一天
     * @return 日志列表
     */
    List<TeachingLog> listByClassWeek(Long classId, LocalDate anyDayOfWeek);
}
