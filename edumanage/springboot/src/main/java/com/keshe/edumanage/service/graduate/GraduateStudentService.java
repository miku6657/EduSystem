package com.keshe.edumanage.service.graduate;

import com.baomidou.mybatisplus.extension.service.IService;
import com.keshe.edumanage.entity.graduate.GraduateStudent;

import java.util.List;

/**
 * 毕业生信息业务接口
 * <p>毕业生管理：编写毕业证编号、生成上报库、打印毕业证数据查询</p>
 */
public interface GraduateStudentService extends IService<GraduateStudent> {

    /**
     * 为单条毕业生记录生成毕业证编号
     * <p>编号规则：毕业年份 + 6 位流水号（取记录ID补零），全局唯一</p>
     *
     * @param id 毕业生记录ID
     * @return 生成的毕业证编号
     */
    String generateCertificateNo(Long id);

    /**
     * 批量为某一届尚未编号的毕业生生成毕业证编号
     *
     * @param graduateYear 毕业年份
     * @return 本次生成编号的记录数
     */
    int batchGenerateCertificateNo(String graduateYear);

    /**
     * 按届查询毕业生列表（用于生成上报库、打印毕业证）
     *
     * @param graduateYear 毕业年份
     * @return 毕业生列表
     */
    List<GraduateStudent> listByYear(String graduateYear);
}
