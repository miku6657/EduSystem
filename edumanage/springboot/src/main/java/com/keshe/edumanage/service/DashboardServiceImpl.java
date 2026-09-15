package com.keshe.edumanage.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.keshe.edumanage.entity.base.ClassroomApply;
import com.keshe.edumanage.entity.graduate.GraduateCheck;
import com.keshe.edumanage.service.base.ClassroomApplyService;
import com.keshe.edumanage.service.base.ClassroomService;
import com.keshe.edumanage.service.base.CourseService;
import com.keshe.edumanage.service.graduate.GraduateCheckService;
import com.keshe.edumanage.vo.DashboardStatisticsVO;
import com.keshe.edumanage.vo.PendingApprovalVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 仪表盘统计业务实现
 */
@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {

    /** 待审核教室申请 */
    private static final String STATUS_PENDING = "待审核";

    /** 毕业审核：待审核 */
    private static final String GRADUATE_WAIT = "WAIT";

    private final CourseService courseService;
    private final ClassroomService classroomService;
    private final GraduateCheckService graduateCheckService;
    private final ClassroomApplyService classroomApplyService;

    @Override
    public DashboardStatisticsVO statistics() {
        DashboardStatisticsVO vo = new DashboardStatisticsVO();

        vo.setCourseCount(courseService.count());
        vo.setClassroomCount(classroomService.count());
        vo.setPendingGraduationCount(
                graduateCheckService.count(
                        Wrappers.<GraduateCheck>lambdaQuery()
                                .eq(GraduateCheck::getCheckStatus, GRADUATE_WAIT)
                )
        );

        // 后端暂无「调课」模块，今日调课申请数暂返回 0，待调课模块建立后补充
        vo.setTodayAdjustCount(0L);

        List<PendingApprovalVO> approvals = classroomApplyService
                .listByStatus(STATUS_PENDING)
                .stream()
                .limit(5)
                .map(this::toPendingApproval)
                .toList();
        vo.setPendingApprovals(approvals);

        return vo;
    }

    private PendingApprovalVO toPendingApproval(ClassroomApply apply) {
        PendingApprovalVO vo = new PendingApprovalVO();
        vo.setId(apply.getId());
        vo.setType("教室申请");
        vo.setTitle(apply.getPurpose());
        vo.setApplicant(apply.getApplicant());
        vo.setApplyTime(apply.getCreateTime());
        vo.setStatus(apply.getStatus());
        return vo;
    }
}
