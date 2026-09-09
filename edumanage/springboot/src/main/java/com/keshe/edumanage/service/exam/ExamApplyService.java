package com.keshe.edumanage.service.exam;

import com.baomidou.mybatisplus.extension.service.IService;
import com.keshe.edumanage.entity.exam.ExamApply;

import java.util.List;

/**
 * 考核方式申报业务接口
 * <p>考核方式由教师（课程负责人）自行申请，经教研室、系主任审核、教务处审批确定。
 * 实体仅有一个状态字段，本服务以「待审核 - 通过 / 驳回」的状态机管理审核流转</p>
 */
public interface ExamApplyService extends IService<ExamApply> {

    /** 申报状态：待审核 */
    String STATUS_WAIT = "WAIT";

    /** 申报状态：审核通过 */
    String STATUS_PASS = "PASS";

    /** 申报状态：审核驳回 */
    String STATUS_FAIL = "FAIL";

    /**
     * 教师提交考核方式申报（同一教师同一课程存在待审核申报时不允许重复提交）
     *
     * @param examApply 申报信息（课程、教师、考核方式、申报理由）
     */
    void apply(ExamApply examApply);

    /**
     * 审核申报（仅待审核状态可审核）
     *
     * @param id     申报记录ID
     * @param status 审核结果：PASS / FAIL
     */
    void audit(Long id, String status);

    /**
     * 按状态查询申报列表（用于导出考核方式总表）
     *
     * @param status 申报状态（为空时查全部）
     * @return 申报列表
     */
    List<ExamApply> listForExport(String status);
}
