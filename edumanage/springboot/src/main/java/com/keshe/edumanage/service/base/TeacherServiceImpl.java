package com.keshe.edumanage.service.base;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.keshe.edumanage.entity.base.Teacher;
import com.keshe.edumanage.mapper.base.TeacherMapper;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 教师信息业务实现
 */
@Service
public class TeacherServiceImpl extends ServiceImpl<TeacherMapper, Teacher>
        implements TeacherService {

    @Override
    public Page<Teacher> pageTeachers(Page<Teacher> page, String name, String type,
                                      Long departmentId, Long teachingGroupId) {
        return lambdaQuery()
                .like(name != null && !name.isBlank(), Teacher::getName, name)
                .eq(type != null && !type.isBlank(), Teacher::getType, type)
                .eq(departmentId != null, Teacher::getDepartmentId, departmentId)
                .eq(teachingGroupId != null, Teacher::getTeachingGroupId, teachingGroupId)
                .orderByDesc(Teacher::getId)
                .page(page);
    }

    @Override
    public List<Teacher> listByDepartment(Long departmentId) {
        return lambdaQuery()
                .eq(Teacher::getDepartmentId, departmentId)
                .list();
    }
}
