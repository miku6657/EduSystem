package com.keshe.edumanage.vo;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 教室申请返回对象
 * <p>字段对齐前端：date ← applyDate、applyTime ← createTime、roomName ← 教室编号</p>
 */
@Data
public class ClassroomApplyVO {

    private Long id;

    private Long roomId;

    private String roomName;

    /**
     * CAS账号
     */
    private String applicant;

    /**
     * 学生/教师真实姓名
     */
    private String applicantName;

    private String className;

    private LocalDate date;

    private String timeSlot;

    private String purpose;

    private String reason;

    private String status;

    private LocalDateTime applyTime;
}
