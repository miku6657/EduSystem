package com.keshe.edumanage.entity.base;
import com.baomidou.mybatisplus.annotation.TableName;
import com.keshe.edumanage.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@TableName("base_course")
@EqualsAndHashCode(callSuper = true)
public class Course extends BaseEntity {
    private String courseCode;
    private String name;
    private Double credit;
    private String type;
    private Long teachingGroupId;
}
