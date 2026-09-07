package com.keshe.edumanage.entity;


import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;

import java.time.LocalDateTime;


/**
 * 实体公共字段
 */
@Data
public class BaseEntity {


    /**
     * 创建时间
     */
    @TableField(
            fill = FieldFill.INSERT
    )
    private LocalDateTime createTime;



    /**
     * 更新时间
     */
    @TableField(
            fill = FieldFill.INSERT_UPDATE
    )
    private LocalDateTime updateTime;

}