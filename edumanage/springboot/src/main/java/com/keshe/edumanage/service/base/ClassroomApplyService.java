package com.keshe.edumanage.service.base;


import com.baomidou.mybatisplus.extension.service.IService;
import com.keshe.edumanage.entity.base.ClassroomApply;

import java.time.LocalDate;
import java.util.List;


public interface ClassroomApplyService
        extends IService<ClassroomApply>{

    /** 申请状态：待审核 */
    String STATUS_WAIT = "待审核";

    /** 申请状态：已通过 */
    String STATUS_PASS = "已通过";

    /** 申请状态：已驳回 */
    String STATUS_FAIL = "已驳回";

    /** 申请状态：已取消（师生端撤回） */
    String STATUS_CANCEL = "已取消";

    /**
     * 判断某教室某天某时段是否已被占用（待审核 / 已通过 均视为占用）
     */
    boolean checkConflict(
            Long roomId,
            LocalDate date,
            String timeSlot
    );

    /**
     * 查询某位申请人（登录名）的申请记录
     */
    List<ClassroomApply> listByApplicant(
            String username
    );

    /**
     * 按状态查询申请列表（status 为空时查全部）
     */
    List<ClassroomApply> listByStatus(
            String status
    );

    /**
     * 师生端撤回自己的申请（仅本人、且仅待审核状态可撤回）
     *
     * @param id       申请ID
     * @param username 当前登录名
     */
    void cancel(
            Long id,
            String username
    );

}