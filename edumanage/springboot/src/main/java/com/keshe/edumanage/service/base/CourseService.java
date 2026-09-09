package com.keshe.edumanage.service.base;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.keshe.edumanage.entity.base.Course;

import java.util.List;

/**
 * 课程信息业务接口
 * <p>课程按学期从鹏达系统读取，以教研室为单位归属管理，
 * 包含课程代码、学分、课程类型、所属教研室等属性</p>
 */
public interface CourseService extends IService<Course> {

    /**
     * 根据课程代码查询课程
     *
     * @param courseCode 课程代码
     * @return 课程信息，不存在时返回 null
     */
    Course getByCourseCode(String courseCode);

    /**
     * 分页条件查询课程
     *
     * @param page            分页参数
     * @param keyword         课程名称/代码关键字（模糊匹配，可为空）
     * @param type            课程类型（可为空）
     * @param teachingGroupId 教研室ID（可为空）
     * @return 课程分页数据
     */
    Page<Course> pageCourses(Page<Course> page, String keyword, String type,
                             Long teachingGroupId);

    /**
     * 查询某教研室的课程
     *
     * @param teachingGroupId 教研室ID
     * @return 课程列表
     */
    List<Course> listByTeachingGroup(Long teachingGroupId);
}
