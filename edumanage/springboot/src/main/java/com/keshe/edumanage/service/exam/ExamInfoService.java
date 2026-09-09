package com.keshe.edumanage.service.exam;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.keshe.edumanage.entity.exam.ExamInfo;

import java.util.List;

/**
 * 考试信息业务接口
 * <p>期末考试安排：确定考场、考试编号、课程、监考员等，
 * 排考时自动做教室与监考教师的时间冲突判断</p>
 */
public interface ExamInfoService extends IService<ExamInfo> {

    /** 考试状态：已安排 */
    String STATUS_ARRANGED = "ARRANGED";

    /**
     * 期末考试安排：保存考试信息，并同步写入考场安排与监考安排。
     * 排考前做冲突判断：同一时间段教室不得被其他考试占用、
     * 监考教师不得在同一时间段已有监考任务
     *
     * @param examInfo          考试信息（名称、课程、学期、类型、日期、起止时间）
     * @param classroomIds      考场教室ID列表（座位数自动取教室容量）
     * @param monitorTeacherIds 监考教师ID列表（第一位为主监考，其余为副监考）
     */
    void arrangeExam(ExamInfo examInfo, List<Long> classroomIds, List<Long> monitorTeacherIds);

    /**
     * 分页条件查询考试信息（面向师生的考试信息查询）
     *
     * @param page     分页参数
     * @param name     考试名称关键字（模糊匹配，可为空）
     * @param termId   学期ID（可为空）
     * @param examType 考试类型（可为空）
     * @return 考试分页数据
     */
    Page<ExamInfo> pageExams(Page<ExamInfo> page, String name, Long termId, String examType);

    /**
     * 查询某学期的考试列表
     *
     * @param termId 学期ID
     * @return 考试列表
     */
    List<ExamInfo> listByTerm(Long termId);

    /**
     * 查询某门课程的考试列表
     *
     * @param courseId 课程ID
     * @return 考试列表
     */
    List<ExamInfo> listByCourse(Long courseId);

    /**
     * 删除考试（连同考场安排、监考安排一并删除）
     *
     * @param id 考试ID
     */
    void removeExam(Long id);
}
