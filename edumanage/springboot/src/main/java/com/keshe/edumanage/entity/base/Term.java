package com.keshe.edumanage.entity.base;


import com.baomidou.mybatisplus.annotation.TableName;
import com.keshe.edumanage.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;


import java.time.LocalDate;


@Data
@TableName("base_term")
@EqualsAndHashCode(callSuper = true)
public class Term extends BaseEntity {


    /**
     * 学期名称
     */
    private String name;


    /**
     * 开始日期
     */
    private LocalDate startDate;


    /**
     * 结束日期
     */
    private LocalDate endDate;


    /**
     * 状态
     */
    private Integer status;

}