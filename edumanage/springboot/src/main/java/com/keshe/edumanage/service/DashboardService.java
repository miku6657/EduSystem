package com.keshe.edumanage.service;

import com.keshe.edumanage.vo.DashboardStatisticsVO;

/**
 * 仪表盘统计业务接口
 */
public interface DashboardService {

    /**
     * 统计后台首页工作台数据
     *
     * @return 统计结果
     */
    DashboardStatisticsVO statistics();
}
