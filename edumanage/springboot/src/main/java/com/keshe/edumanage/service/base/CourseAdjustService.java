package com.keshe.edumanage.service.base;

import com.baomidou.mybatisplus.extension.service.IService;
import com.keshe.edumanage.entity.base.CourseAdjust;

import java.util.List;

/**
 * 调课申请业务接口
 * <p>教师提交调课申请 → 教务处审批；审批前教师可自行撤销。</p>
 */
public interface CourseAdjustService extends IService<CourseAdjust> {

    /** 状态：待审核 */
    String STATUS_WAIT = "待审核";

    /** 状态：已通过 */
    String STATUS_PASS = "已通过";

    /** 状态：已驳回 */
    String STATUS_FAIL = "已驳回";

    /** 状态：已撤销（教师撤回） */
    String STATUS_CANCEL = "已撤销";

    /**
     * 教师提交调课申请
     *
     * @param adjust 申请信息（教师、课程、原时间、调整后时间、原因）
     */
    void submit(CourseAdjust adjust);

    /**
     * 查询某位教师的调课申请（师生端"我的调课"）
     */
    List<CourseAdjust> listByTeacher(Long teacherId);

    /**
     * 按状态查询调课申请（管理端审批列表，status 为空查全部）
     */
    List<CourseAdjust> listByStatus(String status);

    /**
     * 审批通过（仅待审核可审批）
     */
    void approve(Long id, String remark);

    /**
     * 审批驳回（仅待审核可审批）
     */
    void reject(Long id, String remark);

    /**
     * 教师撤销自己的申请（仅本人、仅待审核）
     */
    void cancel(Long id, Long teacherId);
}
