package com.keshe.edumanage.entity.exam;


import com.baomidou.mybatisplus.annotation.TableName;
import com.keshe.edumanage.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;


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

}