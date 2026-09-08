package com.keshe.edumanage.entity.base;


import com.baomidou.mybatisplus.annotation.TableName;
import com.keshe.edumanage.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;


@Data
@TableName("base_teaching_group")
@EqualsAndHashCode(callSuper = true)
public class TeachingGroup extends BaseEntity {


    /**
     * 教研室名称
     */
    private String name;


    /**
     * 所属系部
     */
    private Long departmentId;

}