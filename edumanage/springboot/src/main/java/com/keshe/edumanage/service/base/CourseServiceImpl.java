package com.keshe.edumanage.service.base;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.keshe.edumanage.entity.base.Course;
import com.keshe.edumanage.mapper.base.CourseMapper;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 课程信息业务实现
 */
@Service
public class CourseServiceImpl extends ServiceImpl<CourseMapper, Course>
        implements CourseService {

    @Override
    public Course getByCourseCode(String courseCode) {
        return lambdaQuery()
                .eq(Course::getCourseCode, courseCode)
                .one();
    }

    @Override
    public Page<Course> pageCourses(Page<Course> page, String keyword, String type,
                                    Long teachingGroupId) {
        return lambdaQuery()
                .and(keyword != null && !keyword.isBlank(),
                        w -> w.like(Course::getName, keyword)
                              .or()
                              .like(Course::getCourseCode, keyword))
                .eq(type != null && !type.isBlank(), Course::getType, type)
                .eq(teachingGroupId != null, Course::getTeachingGroupId, teachingGroupId)
                .orderByDesc(Course::getId)
                .page(page);
    }

    @Override
    public List<Course> listByTeachingGroup(Long teachingGroupId) {
        return lambdaQuery()
                .eq(Course::getTeachingGroupId, teachingGroupId)
                .list();
    }
}
