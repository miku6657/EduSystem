package com.keshe.edumanage.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 待办审批条目
 */
@Data
public class PendingApprovalVO {

    private Long id;

    /** 审批类型 */
    private String type;

    /** 审批事项 */
    private String title;

    /** 申请人 */
    private String applicant;

    /** 申请时间 */
    private LocalDateTime applyTime;

    /** 状态 */
    private String status;
}
