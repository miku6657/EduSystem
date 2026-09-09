package com.keshe.edumanage.service.attendance;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.keshe.edumanage.common.exception.BusinessException;
import com.keshe.edumanage.entity.attendance.TeacherAttendance;
import com.keshe.edumanage.mapper.attendance.TeacherAttendanceMapper;
import com.keshe.edumanage.service.base.TeacherService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 教师考勤业务实现
 */
@Service
@RequiredArgsConstructor
public class TeacherAttendanceServiceImpl
        extends ServiceImpl<TeacherAttendanceMapper, TeacherAttendance>
        implements TeacherAttendanceService {

    private final TeacherService teacherService;

    @Override
    public void checkIn(Long teacherId) {
        LocalDate today = LocalDate.now();
        boolean exists = lambdaQuery()
                .eq(TeacherAttendance::getTeacherId, teacherId)
                .eq(TeacherAttendance::getAttendanceDate, today)
                .exists();
        if (exists) {
            throw new BusinessException("今日已签到，请勿重复签到");
        }
        TeacherAttendance attendance = new TeacherAttendance();
        attendance.setTeacherId(teacherId);
        attendance.setAttendanceDate(today);
        attendance.setStatus(STATUS_NORMAL);
        attendance.setCheckTime(LocalDateTime.now());
        save(attendance);
    }

    @Override
    public void importAttendance(Long teacherId, LocalDate attendanceDate, String status,
                                 LocalDateTime checkTime) {
        TeacherAttendance exist = lambdaQuery()
                .eq(TeacherAttendance::getTeacherId, teacherId)
                .eq(TeacherAttendance::getAttendanceDate, attendanceDate)
                .one();
        if (exist == null) {
            TeacherAttendance attendance = new TeacherAttendance();
            attendance.setTeacherId(teacherId);
            attendance.setAttendanceDate(attendanceDate);
            attendance.setStatus(status);
            attendance.setCheckTime(checkTime);
            save(attendance);
        } else {
            exist.setStatus(status);
            exist.setCheckTime(checkTime);
            updateById(exist);
        }
    }

    @Override
    public List<TeacherAttendance> listByDate(LocalDate date) {
        return lambdaQuery()
                .eq(TeacherAttendance::getAttendanceDate, date)
                .list();
    }

    @Override
    public Map<String, Object> statByDate(LocalDate date) {
        List<TeacherAttendance> list = listByDate(date);
        long totalTeachers = teacherService.count();

        Map<String, Object> result = new HashMap<>();
        result.put("total", totalTeachers);          // 教师总数
        result.put("checked", list.size());          // 已签到人数
        result.put("absent", totalTeachers - list.size()); // 未签到人数
        return result;
    }
}
