package com.keshe.edumanage.entity.base;
import com.baomidou.mybatisplus.annotation.TableName;
import com.keshe.edumanage.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@TableName("base_teacher")
@EqualsAndHashCode(callSuper = true)
public class Teacher extends BaseEntity {
    private String teacherNo;
    private String name;
    private String gender;
    private String type;
    private String phone;
    private Long departmentId;
    private Long teachingGroupId;
}
