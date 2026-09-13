package com.keshe.edumanage.entity.exam;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.keshe.edumanage.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;

@Data
@TableName("exam_score")
@EqualsAndHashCode(callSuper = true)
public class ExamScore extends BaseEntity {
    private Long examId;
    private Long studentId;
    private Double score;
    private String status;

    /**
     * 以下为展示用字段：不属于 exam_score 表，
     * 由 ExamScoreService 关联 exam_info / base_course / base_student 填充，
     * 目的是让前端不必再按 ID 逐个反查。
     */
    @TableField(exist = false)
    private String studentName;

    @TableField(exist = false)
    private String studentNo;

    @TableField(exist = false)
    private String examName;

    @TableField(exist = false)
    private String courseName;

    @TableField(exist = false)
    private LocalDate examDate;

    @TableField(exist = false)
    private Double credit;
}
