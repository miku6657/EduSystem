package com.keshe.edumanage.entity.base;
import com.baomidou.mybatisplus.annotation.TableName;
import com.keshe.edumanage.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@TableName("base_classroom")
@EqualsAndHashCode(callSuper = true)
public class Classroom extends BaseEntity {
    private String roomNo;
    private Long campusId;
    private String type;
    private Double area;
    private Integer capacity;
    private String status;
}
