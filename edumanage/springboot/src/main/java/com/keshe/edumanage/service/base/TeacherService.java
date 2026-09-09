package com.keshe.edumanage.service.base;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.keshe.edumanage.entity.base.Teacher;

import java.util.List;

/**
 * 教师信息业务接口
 * <p>教师基础数据由组织人事部管理和维护，支持按类型分类查询
 * （专职教师、校内兼职教师、校外兼职教师）</p>
 */
public interface TeacherService extends IService<Teacher> {

    /**
     * 分页条件查询教师
     *
     * @param page             分页参数
     * @param name             姓名关键字（模糊匹配，可为空）
     * @param type             教师类型：专职/校内兼职/校外兼职（可为空）
     * @param departmentId     系部ID（可为空）
     * @param teachingGroupId  教研室ID（可为空）
     * @return 教师分页数据
     */
    Page<Teacher> pageTeachers(Page<Teacher> page, String name, String type,
                               Long departmentId, Long teachingGroupId);

    /**
     * 查询某系部下的全部教师
     *
     * @param departmentId 系部ID
     * @return 教师列表
     */
    List<Teacher> listByDepartment(Long departmentId);
}
