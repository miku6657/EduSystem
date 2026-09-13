package com.keshe.edumanage.entity.graduate;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.keshe.edumanage.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@TableName("upgrade_apply")
@EqualsAndHashCode(callSuper = true)
public class UpgradeApply extends BaseEntity {
    private Long studentId;
    private String schoolName;
    private String majorName;
    private String applyStatus;
    private String remark;

    /**
     * 以下为展示用字段（不属于 upgrade_apply 表），由 Service 关联 base_student 填充。
     */
    @TableField(exist = false)
    private String studentName;

    @TableField(exist = false)
    private String studentNo;

    @TableField(exist = false)
    private String className;
}
