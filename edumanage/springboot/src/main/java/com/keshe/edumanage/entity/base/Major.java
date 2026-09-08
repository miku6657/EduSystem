package com.keshe.edumanage.entity.base;
import com.baomidou.mybatisplus.annotation.TableName;
import com.keshe.edumanage.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@TableName("base_major")
@EqualsAndHashCode(callSuper = true)
public class Major extends BaseEntity {
    /**
     * 专业名称
     */
    private String name;

    /**
     * 专业代码
     */
    private String code;

    /**
     * 系部ID
     */
    private Long departmentId;
}
