package com.keshe.edumanage.entity.base;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.keshe.edumanage.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 教学任务（任课关系 + 上课时间）
 * <p>一条记录表示「某教师在某学期教某班级的某门课程，在星期几第几节、哪个教室上课」，
 * 是师生端「我的课程 / 我的教学任务 / 课表」与教师成绩录入权限的判断依据。</p>
 */
@Data
@TableName("base_teaching_task")
@EqualsAndHashCode(callSuper = true)
public class TeachingTask extends BaseEntity {
    /**
     * 教师ID
     */
    private Long teacherId;

    /**
     * 课程ID
     */
    private Long courseId;

    /**
     * 班级ID
     */
    private Long classId;

    /**
     * 学期ID
     */
    private Long termId;

    /**
     * 星期几（1=周一 … 7=周日），为空表示尚未排课
     */
    private Integer weekday;

    /**
     * 开始节次
     */
    private Integer startSection;

    /**
     * 结束节次
     */
    private Integer endSection;

    /**
     * 上课教室ID
     */
    private Long classroomId;

    /**
     * 上课周次，如 1-16周
     */
    private String weeks;

    /**
     * 以下为展示用字段（不属于 base_teaching_task 表），由 Service 关联填充。
     */
    @TableField(exist = false)
    private String courseName;

    @TableField(exist = false)
    private String className;

    @TableField(exist = false)
    private String teacherName;

    @TableField(exist = false)
    private String roomName;
}
