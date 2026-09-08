package com.keshe.edumanage.entity.system;
import com.baomidou.mybatisplus.annotation.TableName;
import com.keshe.edumanage.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@TableName("sys_role")
@EqualsAndHashCode(callSuper = true)
public class Role extends BaseEntity {
    /**
     * 角色名称
     */
    private String roleName;

    /**
     * 角色编码
     */
    private String roleCode;
}
