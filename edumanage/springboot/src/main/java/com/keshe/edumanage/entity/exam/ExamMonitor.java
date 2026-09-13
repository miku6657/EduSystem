package com.keshe.edumanage.entity.exam;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.keshe.edumanage.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
@TableName("exam_monitor")
@EqualsAndHashCode(callSuper = true)
public class ExamMonitor extends BaseEntity {
    private Long examId;
    private Long teacherId;

    /**
     * 主监考/副监考
     */
    private String monitorRole;

    /**
     * 以下为展示用字段（不属于 exam_monitor 表），
     * 由 ExamMonitorService 关联 exam_info / exam_room / base_classroom / base_teacher 填充。
     */
    @TableField(exist = false)
    private String examName;

    @TableField(exist = false)
    private String courseName;

    @TableField(exist = false)
    private LocalDate examDate;

    @TableField(exist = false)
    private LocalTime startTime;

    @TableField(exist = false)
    private LocalTime endTime;

    /**
     * 考场名称（同一场考试有多个考场时以顿号连接）
     */
    @TableField(exist = false)
    private String roomName;

    @TableField(exist = false)
    private String teacherName;
}
