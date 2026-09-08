package com.keshe.edumanage.entity.base;


import com.baomidou.mybatisplus.annotation.TableName;
import com.keshe.edumanage.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;


@Data
@TableName("base_class")
@EqualsAndHashCode(callSuper = true)
public class ClassInfo extends BaseEntity {


    /**
     * 班级名称
     */
    private String name;


    /**
     * 专业ID
     */
    private Long majorId;


    /**
     * 校区ID
     */
    private Long campusId;


    /**
     * 年级
     */
    private String grade;


    /**
     * 人数
     */
    private Integer studentCount;


    /**
     * 辅导员
     */
    private String counselor;

}