package com.keshe.edumanage.entity.base;
import com.baomidou.mybatisplus.annotation.TableName;
import com.keshe.edumanage.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@TableName("base_department")
@EqualsAndHashCode(callSuper = true)
public class Department extends BaseEntity {
    private String name;
    private String code;
}
