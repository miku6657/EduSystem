package com.keshe.edumanage.entity.graduate;


import com.baomidou.mybatisplus.annotation.TableName;
import com.keshe.edumanage.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;


@Data
@TableName("graduate_check")
@EqualsAndHashCode(callSuper = true)
public class GraduateCheck extends BaseEntity {


    private Long studentId;


    private String checkStatus;


    private String creditStatus;


    private String courseStatus;


    private String remark;


    private String checker;

}