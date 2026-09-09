package com.keshe.edumanage.service.base;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.keshe.edumanage.entity.base.Classroom;

import java.util.List;

/**
 * 教室信息业务接口
 * <p>教室按学期从鹏达系统读取，包含所属校区、类型、面积、座位数、
 * 当前使用状况（占用/空闲）等属性</p>
 */
public interface ClassroomService extends IService<Classroom> {

    /**
     * 分页条件查询教室
     *
     * @param page     分页参数
     * @param roomNo   教室编号关键字（模糊匹配，可为空）
     * @param campusId 校区ID（可为空）
     * @param type     教室类型：普通教室/机房/多媒体/实验室等（可为空）
     * @param status   使用状况：占用/空闲（可为空）
     * @return 教室分页数据
     */
    Page<Classroom> pageClassrooms(Page<Classroom> page, String roomNo, Long campusId,
                                   String type, String status);

    /**
     * 查询某校区下指定类型的空闲教室（用于排考/上课等场景）
     *
     * @param campusId 校区ID（可为空）
     * @param type     教室类型（可为空）
     * @return 空闲教室列表
     */
    List<Classroom> listFreeClassrooms(Long campusId, String type);
}
