package com.keshe.edumanage.entity.exam;
import com.baomidou.mybatisplus.annotation.TableField;
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
    private Long examId;

    /**
     * 补考/重修
     */
    private String type;

    /**
     * 以下为展示用字段（不属于 exam_retake 表），由 Service 关联 base_course / base_student / exam_info 填充。
     */
    @TableField(exist = false)
    private String courseName;

    @TableField(exist = false)
    private String studentName;

    @TableField(exist = false)
    private String studentNo;

    /**
     * 已安排场次的考试名称（examId 为空时为 null）
     */
    @TableField(exist = false)
    private String examName;
}
