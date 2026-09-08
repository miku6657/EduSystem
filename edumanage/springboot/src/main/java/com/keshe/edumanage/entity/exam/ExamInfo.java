package com.keshe.edumanage.entity.exam;


import com.baomidou.mybatisplus.annotation.TableName;
import com.keshe.edumanage.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;
import java.time.LocalTime;


@Data
@TableName("exam_info")
@EqualsAndHashCode(callSuper = true)
public class ExamInfo extends BaseEntity {


    /**
     * 考试名称
     */
    private String name;


    /**
     * 课程ID
     */
    private Long courseId;


    /**
     * 学期ID
     */
    private Long termId;


    /**
     * 考试类型
     */
    private String examType;


    /**
     * 考试日期
     */
    private LocalDate examDate;


    /**
     * 开始时间
     */
    private LocalTime startTime;


    /**
     * 结束时间
     */
    private LocalTime endTime;


    /**
     * 状态
     */
    private String status;

}