package com.keshe.edumanage.service.exam;

import com.baomidou.mybatisplus.extension.service.IService;
import com.keshe.edumanage.entity.exam.ExamMonitor;

import java.util.List;

/**
 * 监考安排业务接口
 * <p>维护监考教师安排（主监考/副监考）</p>
 */
public interface ExamMonitorService extends IService<ExamMonitor> {

    /**
     * 查询某场考试的监考安排
     *
     * @param examId 考试ID
     * @return 监考安排列表
     */
    List<ExamMonitor> listByExam(Long examId);

    /**
     * 查询某位教师的监考任务
     *
     * @param teacherId 教师ID
     * @return 监考安排列表
     */
    List<ExamMonitor> listByTeacher(Long teacherId);

    /**
     * 删除某场考试的全部监考安排
     *
     * @param examId 考试ID
     */
    void removeByExam(Long examId);
}
