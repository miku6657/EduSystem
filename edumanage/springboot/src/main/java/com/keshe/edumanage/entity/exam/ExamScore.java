package com.keshe.edumanage.entity.exam;


import com.baomidou.mybatisplus.annotation.TableName;
import com.keshe.edumanage.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;


@Data
@TableName("exam_score")
@EqualsAndHashCode(callSuper = true)
public class ExamScore extends BaseEntity {


    private Long examId;


    private Long studentId;


    private Double score;


    private String status;

}