package com.keshe.edumanage.entity.base;


import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;


@Data
@TableName("classroom_apply")
public class ClassroomApply {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long roomId;
    private String applicant;
    private String className;
    private LocalDate applyDate;
    private String timeSlot;
    private String purpose;
    private String reason;
    private String status;
    private LocalDateTime createTime;
}