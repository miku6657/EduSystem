package com.keshe.edumanage.entity.graduate;
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
}
