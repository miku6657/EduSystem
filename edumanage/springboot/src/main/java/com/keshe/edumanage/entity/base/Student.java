package com.keshe.edumanage.entity.base;
import com.baomidou.mybatisplus.annotation.TableName;
import com.keshe.edumanage.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@TableName("base_student")
@EqualsAndHashCode(callSuper = true)
public class Student extends BaseEntity {
    private String studentNo;
    private String name;
    private String gender;
    private Long classId;
    private String phone;
    private String status;
}
