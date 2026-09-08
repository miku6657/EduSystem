package com.keshe.edumanage.entity.graduate;


import com.baomidou.mybatisplus.annotation.TableName;
import com.keshe.edumanage.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;


@Data
@TableName("graduate_student")
@EqualsAndHashCode(callSuper = true)
public class GraduateStudent extends BaseEntity {


    private Long studentId;


    private String graduateYear;


    private String certificateNo;


    private String graduateStatus;

}