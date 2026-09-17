package com.keshe.edumanage.service.exam;

import com.baomidou.mybatisplus.extension.service.IService;
import com.keshe.edumanage.dto.ExamArrangeDTO;
import com.keshe.edumanage.entity.exam.ExamRetake;
import com.keshe.edumanage.entity.exam.ExamScore;

import java.util.List;

public interface ExamRetakeService
        extends IService<ExamRetake> {

    String TYPE_MAKEUP = "补考";

    String TYPE_RETAKE = "重修";

    String STATUS_WAIT = "WAIT";

    String STATUS_APPROVED = "APPROVED";

    String STATUS_REJECTED = "REJECTED";

    String STATUS_ARRANGED = "ARRANGED";

    String STATUS_COMPLETED = "COMPLETED";

    /**
     * 学生申请重修。
     */
    void applyRetake(
            Long studentId,
            Long courseId
    );

    /**
     * 管理员通过申请。
     */
    void approve(Long id);

    /**
     * 管理员驳回申请。
     */
    void reject(Long id);

    /**
     * 给已经批准的申请安排考试。
     */
    void assignExam(
            Long id,
            Long examId
    );

    /**
     * 为已批准的重修申请创建专门的重修考试。
     *
     * APPROVED -> ARRANGED
     */
    void arrangeRetakeExam(
            Long id,
            ExamArrangeDTO dto
    );

    /**
     * 学生自己的申请。
     */
    List<ExamRetake> listByStudent(
            Long studentId
    );

    /**
     * 按类型查询。
     */
    List<ExamRetake> listByType(
            String type
    );

    /**
     * 查询某场重修考试的学生。
     */
    List<ExamRetake> listByExam(
            Long examId
    );

    /**
     * 教师录入重修考试成绩。
     *
     * 不覆盖原期末考试成绩，
     * 而是为本次重修考试新增一条成绩记录。
     */
    void saveRetakeScores(
            Long examId,
            List<ExamScore> scores
    );
}
