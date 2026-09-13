package com.keshe.edumanage.entity.base;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.keshe.edumanage.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;

/**
 * 调课申请
 * <p>教师提交「原上课时间 → 调整后时间」的调课申请，由教务处审批。</p>
 */
@Data
@TableName("course_adjust")
@EqualsAndHashCode(callSuper = true)
public class CourseAdjust extends BaseEntity {
    /**
     * 申请教师ID
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
     * 原上课日期
     */
    private LocalDate originDate;

    /**
     * 原上课时段
     */
    private String originSlot;

    /**
     * 调整后上课日期
     */
    private LocalDate targetDate;

    /**
     * 调整后上课时段
     */
    private String targetSlot;

    /**
     * 调整后教室ID（可为空，表示教室不变）
     */
    private Long classroomId;

    /**
     * 调课原因
     */
    private String reason;

    /**
     * 状态：待审核/已通过/已驳回/已撤销
     */
    private String status;

    /**
     * 审批意见
     */
    private String approveRemark;

    /**
     * 以下为展示用字段（不属于 course_adjust 表），由 Service 关联填充。
     */
    @TableField(exist = false)
    private String teacherName;

    @TableField(exist = false)
    private String courseName;

    @TableField(exist = false)
    private String className;

    @TableField(exist = false)
    private String roomName;
}
