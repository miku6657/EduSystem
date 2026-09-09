package com.keshe.edumanage.service.exam;

import com.baomidou.mybatisplus.extension.service.IService;
import com.keshe.edumanage.entity.exam.ExamRoom;

import java.util.List;

/**
 * 考场安排业务接口
 * <p>维护考试与教室的关联关系</p>
 */
public interface ExamRoomService extends IService<ExamRoom> {

    /**
     * 查询某场考试的考场安排
     *
     * @param examId 考试ID
     * @return 考场列表
     */
    List<ExamRoom> listByExam(Long examId);

    /**
     * 删除某场考试的全部考场安排
     *
     * @param examId 考试ID
     */
    void removeByExam(Long examId);
}
