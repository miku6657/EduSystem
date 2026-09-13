package com.keshe.edumanage.service.base;

import com.baomidou.mybatisplus.extension.service.IService;
import com.keshe.edumanage.entity.base.ClassInfo;
import com.keshe.edumanage.entity.base.Course;
import com.keshe.edumanage.entity.base.TeachingTask;

import java.util.List;

/**
 * 教学任务（任课关系）业务接口
 * <p>教学任务由教务处维护，用于确定教师任教哪些班级的哪些课程</p>
 */
public interface TeachingTaskService extends IService<TeachingTask> {

    /**
     * 查询某教师的全部教学任务
     *
     * @param teacherId 教师ID
     * @param termId    学期ID（可为空，为空查全部学期）
     * @return 教学任务列表
     */
    List<TeachingTask> listByTeacher(Long teacherId, Long termId);

    /**
     * 查询某班级的全部教学任务（师生端「我的课表」数据来源）
     *
     * @param classId 班级ID
     * @param termId  学期ID（可为空）
     * @return 教学任务列表（含课程/教师/教室名称与上课时间）
     */
    List<TeachingTask> listByClass(Long classId, Long termId);

    /**
     * 教师任教的班级列表（按班级去重）
     *
     * @param teacherId 教师ID
     * @param termId    学期ID（可为空）
     * @return 班级列表
     */
    List<ClassInfo> listMyClasses(Long teacherId, Long termId);

    /**
     * 教师任教的课程列表（按课程去重）
     *
     * @param teacherId 教师ID
     * @param termId    学期ID（可为空）
     * @return 课程列表
     */
    List<Course> listMyCourses(Long teacherId, Long termId);

    /**
     * 判断教师是否任教某门课程（成绩录入等写操作的权限依据）
     *
     * @param teacherId 教师ID
     * @param courseId  课程ID
     * @return true 表示该教师任教此课程
     */
    boolean teachesCourse(Long teacherId, Long courseId);
}
