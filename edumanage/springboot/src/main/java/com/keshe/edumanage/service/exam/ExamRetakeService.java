package com.keshe.edumanage.service.exam;

import com.baomidou.mybatisplus.extension.service.IService;
import com.keshe.edumanage.entity.exam.ExamRetake;

import java.util.List;

/**
 * 补考重修业务接口
 * <p>补考安排：从鹏达读取数据后安排补考场次；
 * 重修考试：学生网上申请，教务批准后安排考试</p>
 */
public interface ExamRetakeService extends IService<ExamRetake> {

    /** 类型：补考 */
    String TYPE_MAKEUP = "补考";

    /** 类型：重修 */
    String TYPE_RETAKE = "重修";

    /**
     * 学生申请重修（同一课程已有申请时不允许重复申请，
     * 且该课程历史成绩及格时不允许申请）
     *
     * @param studentId 学生ID
     * @param courseId  课程ID
     */
    void applyRetake(Long studentId, Long courseId);

    /**
     * 安排补考/重修考试（将记录关联到具体考试场次）
     *
     * @param id     补考重修记录ID
     * @param examId 考试ID
     */
    void assignExam(Long id, Long examId);

    /**
     * 查询某位学生的补考重修记录
     *
     * @param studentId 学生ID
     * @return 补考重修记录列表
     */
    List<ExamRetake> listByStudent(Long studentId);

    /**
     * 按类型查询记录
     *
     * @param type 类型：补考 / 重修
     * @return 补考重修记录列表
     */
    List<ExamRetake> listByType(String type);
}
