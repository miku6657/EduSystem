package com.keshe.edumanage.entity.exam;
import com.baomidou.mybatisplus.annotation.TableName;
import com.keshe.edumanage.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@TableName("exam_apply")
@EqualsAndHashCode(callSuper = true)
public class ExamApply extends BaseEntity {
    private Long courseId;
    private Long teacherId;

    /**
     * 考核方式
     */
    private String applyType;
    private String reason;

    /**
     * WAIT/PASS/FAIL
     */
    private String status;
}
