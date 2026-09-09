package com.keshe.edumanage.service.base;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.keshe.edumanage.entity.base.Student;
import com.keshe.edumanage.mapper.base.StudentMapper;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 学生信息业务实现
 */
@Service
public class StudentServiceImpl extends ServiceImpl<StudentMapper, Student>
        implements StudentService {

    @Override
    public Student getByStudentNo(String studentNo) {
        return lambdaQuery()
                .eq(Student::getStudentNo, studentNo)
                .one();
    }

    @Override
    public Page<Student> pageStudents(Page<Student> page, String keyword, Long classId,
                                      String status) {
        return lambdaQuery()
                .and(keyword != null && !keyword.isBlank(),
                        w -> w.like(Student::getName, keyword)
                              .or()
                              .like(Student::getStudentNo, keyword))
                .eq(classId != null, Student::getClassId, classId)
                .eq(status != null && !status.isBlank(), Student::getStatus, status)
                .orderByDesc(Student::getId)
                .page(page);
    }

    @Override
    public List<Student> listByClass(Long classId) {
        return lambdaQuery()
                .eq(Student::getClassId, classId)
                .list();
    }
}
