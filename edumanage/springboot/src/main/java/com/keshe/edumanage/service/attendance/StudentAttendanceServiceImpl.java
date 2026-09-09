package com.keshe.edumanage.service.attendance;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.keshe.edumanage.entity.attendance.StudentAttendance;
import com.keshe.edumanage.entity.base.Student;
import com.keshe.edumanage.mapper.attendance.StudentAttendanceMapper;
import com.keshe.edumanage.service.base.StudentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 学生考勤业务实现
 */
@Service
@RequiredArgsConstructor
public class StudentAttendanceServiceImpl
        extends ServiceImpl<StudentAttendanceMapper, StudentAttendance>
        implements StudentAttendanceService {

    private final StudentService studentService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void recordBatch(List<StudentAttendance> records) {
        for (StudentAttendance record : records) {
            StudentAttendance exist = lambdaQuery()
                    .eq(StudentAttendance::getStudentId, record.getStudentId())
                    .eq(StudentAttendance::getCourseId, record.getCourseId())
                    .eq(StudentAttendance::getAttendanceDate, record.getAttendanceDate())
                    .one();
            if (exist == null) {
                record.setId(null);
                save(record);
            } else {
                exist.setStatus(record.getStatus());
                updateById(exist);
            }
        }
    }

    @Override
    public List<StudentAttendance> listByStudent(Long studentId, LocalDate startDate,
                                                 LocalDate endDate) {
        return lambdaQuery()
                .eq(StudentAttendance::getStudentId, studentId)
                .between(StudentAttendance::getAttendanceDate, startDate, endDate)
                .orderByAsc(StudentAttendance::getAttendanceDate)
                .list();
    }

    @Override
    public List<Map<String, Object>> weeklyReport(Long classId, LocalDate anyDayOfWeek) {
        LocalDate monday = anyDayOfWeek.with(DayOfWeek.MONDAY);
        LocalDate sunday = monday.plusDays(6);

        List<Student> students = studentService.listByClass(classId);
        List<Map<String, Object>> report = new ArrayList<>();
        for (Student student : students) {
            List<StudentAttendance> list = listByStudent(student.getId(), monday, sunday);
            long normal = list.stream()
                    .filter(s -> STATUS_NORMAL.equals(s.getStatus()))
                    .count();

            Map<String, Object> row = new HashMap<>();
            row.put("studentNo", student.getStudentNo());
            row.put("name", student.getName());
            row.put("total", list.size());
            row.put("normal", normal);
            row.put("absent", list.size() - normal);
            report.add(row);
        }
        return report;
    }
}
