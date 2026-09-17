package com.keshe.edumanage.entity.exam;

import com.baomidou.mybatisplus.annotation.TableName;
import com.keshe.edumanage.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@TableName("exam_retake")
@EqualsAndHashCode(callSuper = true)
public class ExamRetake extends BaseEntity {

    private Long studentId;

    private Long courseId;

    /**
     * 管理员安排的考试ID。
     * 尚未安排时为空。
     */
    private Long examId;

    /**
     * 补考 / 重修
     */
    private String type;

    /**
     * WAIT
     * APPROVED
     * REJECTED
     * ARRANGED
     * COMPLETED
     */
    private String status;
}