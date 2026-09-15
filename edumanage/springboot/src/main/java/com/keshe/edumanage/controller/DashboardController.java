package com.keshe.edumanage.controller;

import com.keshe.edumanage.common.result.Result;
import com.keshe.edumanage.service.DashboardService;
import com.keshe.edumanage.vo.DashboardStatisticsVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 仪表盘统计接口
 */
@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    /**
     * 后台首页统计
     * GET /api/dashboard/statistics
     */
    @GetMapping("/statistics")
    public Result<DashboardStatisticsVO> statistics() {
        return Result.success(dashboardService.statistics());
    }
}
