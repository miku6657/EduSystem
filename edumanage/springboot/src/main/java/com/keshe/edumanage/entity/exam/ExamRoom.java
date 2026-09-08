package com.keshe.edumanage.entity.exam;


import com.baomidou.mybatisplus.annotation.TableName;
import com.keshe.edumanage.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;


@Data
@TableName("exam_room")
@EqualsAndHashCode(callSuper = true)
public class ExamRoom extends BaseEntity {


    private Long examId;


    private Long classroomId;


    private Integer seatCount;

}