package com.keshe.edumanage.service.base;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.keshe.edumanage.entity.base.ClassInfo;
import com.keshe.edumanage.entity.base.Course;
import com.keshe.edumanage.entity.base.TeachingTask;
import com.keshe.edumanage.mapper.base.TeachingTaskMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 教学任务（任课关系）业务实现
 */
@Service
@RequiredArgsConstructor
public class TeachingTaskServiceImpl extends ServiceImpl<TeachingTaskMapper, TeachingTask>
        implements TeachingTaskService {

    private final ClassInfoService classInfoService;
    private final CourseService courseService;

    @Override
    public List<TeachingTask> listByTeacher(Long teacherId, Long termId) {
        return lambdaQuery()
                .eq(TeachingTask::getTeacherId, teacherId)
                .eq(termId != null, TeachingTask::getTermId, termId)
                .orderByAsc(TeachingTask::getId)
                .list();
    }

    @Override
    public List<ClassInfo> listMyClasses(Long teacherId, Long termId) {
        List<Long> classIds = listByTeacher(teacherId, termId).stream()
                .map(TeachingTask::getClassId)
                .distinct()
                .toList();
        if (classIds.isEmpty()) {
            return List.of();
        }
        return classInfoService.listByIds(classIds);
    }

    @Override
    public List<Course> listMyCourses(Long teacherId, Long termId) {
        List<Long> courseIds = listByTeacher(teacherId, termId).stream()
                .map(TeachingTask::getCourseId)
                .distinct()
                .toList();
        if (courseIds.isEmpty()) {
            return List.of();
        }
        return courseService.listByIds(courseIds);
    }

    @Override
    public boolean teachesCourse(Long teacherId, Long courseId) {
        if (teacherId == null || courseId == null) {
            return false;
        }
        return lambdaQuery()
                .eq(TeachingTask::getTeacherId, teacherId)
                .eq(TeachingTask::getCourseId, courseId)
                .exists();
    }
}
