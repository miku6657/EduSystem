package com.keshe.edumanage.service.base;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.keshe.edumanage.entity.base.Student;

import java.util.List;

/**
 * 学生信息业务接口
 * <p>学生基础数据的维护与查询</p>
 */
public interface StudentService extends IService<Student> {

    /**
     * 根据学号查询学生
     *
     * @param studentNo 学号
     * @return 学生信息，不存在时返回 null
     */
    Student getByStudentNo(String studentNo);

    /**
     * 分页条件查询学生
     *
     * @param page    分页参数
     * @param keyword 姓名/学号关键字（模糊匹配，可为空）
     * @param classId 班级ID（可为空）
     * @param status  学籍状态（可为空）
     * @return 学生分页数据
     */
    Page<Student> pageStudents(Page<Student> page, String keyword, Long classId, String status);

    /**
     * 查询某班级下的全部学生
     *
     * @param classId 班级ID
     * @return 学生列表
     */
    List<Student> listByClass(Long classId);
}
