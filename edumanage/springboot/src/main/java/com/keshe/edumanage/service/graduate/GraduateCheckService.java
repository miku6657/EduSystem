package com.keshe.edumanage.service.graduate;

import com.baomidou.mybatisplus.extension.service.IService;
import com.keshe.edumanage.entity.graduate.GraduateCheck;

import java.util.List;

/**
 * 毕业资格审核业务接口
 * <p>完成学生毕业条件审核：学分审核、课程审核，最终给出毕业资格结论</p>
 */
public interface GraduateCheckService extends IService<GraduateCheck> {

    /** 审核状态：待审核 */
    String STATUS_WAIT = "WAIT";

    /** 审核状态：通过 */
    String STATUS_PASS = "PASS";

    /** 审核状态：不通过 */
    String STATUS_FAIL = "FAIL";

    /**
     * 保存审核结果（同一学生已有审核记录时覆盖更新；
     * 学分或课程审核未通过时，毕业资格结论不允许为通过）
     *
     * @param check 审核信息（学生ID、学分状态、课程状态、结论、备注、审核人）
     */
    void audit(GraduateCheck check);

    /**
     * 按审核结论查询记录
     *
     * @param checkStatus 审核结论：WAIT / PASS / FAIL（为空时查全部）
     * @return 审核记录列表
     */
    List<GraduateCheck> listByStatus(String checkStatus);

    /**
     * 查询某位学生的审核记录
     *
     * @param studentId 学生ID
     * @return 审核记录，不存在时返回 null
     */
    GraduateCheck getByStudent(Long studentId);
}
