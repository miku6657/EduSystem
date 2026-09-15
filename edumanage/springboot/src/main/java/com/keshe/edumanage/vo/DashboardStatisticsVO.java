package com.keshe.edumanage.vo;

import lombok.Data;

import java.util.List;

/**
 * 仪表盘统计返回对象
 */
@Data
public class DashboardStatisticsVO {

    /** 课程总数 */
    private Long courseCount;

    /** 教室总数 */
    private Long classroomCount;

    /** 今日调课申请数（后端暂无调课模块，暂返回 0） */
    private Long todayAdjustCount;

    /** 待审核毕业人数 */
    private Long pendingGraduationCount;

    /** 待办审批列表 */
    private List<PendingApprovalVO> pendingApprovals;
}
