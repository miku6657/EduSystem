package com.keshe.edumanage.service.base;

import com.baomidou.mybatisplus.extension.service.IService;
import com.keshe.edumanage.entity.base.ClassroomApply;
import com.keshe.edumanage.vo.ClassroomApplyVO;

import java.time.LocalDate;
import java.util.List;

/**
 * 教室申请业务接口
 */
public interface ClassroomApplyService
        extends IService<ClassroomApply> {

    boolean checkConflict(
            Long roomId,
            LocalDate date,
            String timeSlot
    );

    List<ClassroomApply> listByApplicant(
            String username
    );

    /**
     * 按状态查询教室申请（status 为空查全部）
     */
    List<ClassroomApply> listByStatus(String status);

    /**
     * 查询申请列表（返回 VO，含教室名称）
     */
    List<ClassroomApplyVO> listVO(String status);

    /**
     * 查询我的申请（返回 VO，含教室名称）
     */
    List<ClassroomApplyVO> listByApplicantVO(String username);
}
