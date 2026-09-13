package com.keshe.edumanage.entity.base;
import com.baomidou.mybatisplus.annotation.TableName;
import com.keshe.edumanage.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 教学任务（任课关系）
 * <p>一条记录表示「某教师在某学期教某班级的某门课程」，
 * 是师生端「我的班级 / 我的课程」与教师成绩录入权限的判断依据。</p>
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
}
