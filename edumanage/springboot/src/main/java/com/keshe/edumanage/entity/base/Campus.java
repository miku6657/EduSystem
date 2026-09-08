package com.keshe.edumanage.entity.base;


import com.baomidou.mybatisplus.annotation.TableName;
import com.keshe.edumanage.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;


@Data
@TableName("base_campus")
@EqualsAndHashCode(callSuper = true)
public class Campus extends BaseEntity {


    /**
     * 校区名称
     */
    private String name;


    /**
     * 地址
     */
    private String address;

}