package com.keshe.edumanage.service.base;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.keshe.edumanage.entity.base.ClassInfo;

import java.util.List;

/**
 * 班级信息业务接口
 * <p>班级按学期从鹏达系统读取，包含所属系部、专业、校区、人数、辅导员等</p>
 */
public interface ClassInfoService extends IService<ClassInfo> {

    /**
     * 分页条件查询班级
     *
     * @param page     分页参数
     * @param name     班级名称关键字（模糊匹配，可为空）
     * @param majorId  专业ID（可为空）
     * @param campusId 校区ID（可为空）
     * @param grade    年级（可为空）
     * @return 班级分页数据
     */
    Page<ClassInfo> pageClasses(Page<ClassInfo> page, String name, Long majorId,
                                Long campusId, String grade);

    /**
     * 查询某专业下的班级列表
     *
     * @param majorId 专业ID
     * @return 班级列表
     */
    List<ClassInfo> listByMajor(Long majorId);
}
