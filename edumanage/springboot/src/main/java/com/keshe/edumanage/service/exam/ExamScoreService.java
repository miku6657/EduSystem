package com.keshe.edumanage.service.exam;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.keshe.edumanage.entity.exam.ExamScore;

import java.util.List;
import java.util.Map;

/**
 * 考试成绩业务接口
 * <p>成绩录入与考试数据统计（应考人数、实考人数、缺考人数等）</p>
 */
public interface ExamScoreService extends IService<ExamScore> {

    /** 成绩状态：缺考 */
    String STATUS_ABSENT = "ABSENT";

    /** 及格分数线 */
    double PASS_SCORE = 60;

    /**
     * 批量录入成绩（同一学生同一考试已有成绩记录时更新分数）
     *
     * @param examId 考试ID
     * @param scores 成绩列表（学生ID、分数、状态）
     */
    void saveScores(Long examId, List<ExamScore> scores);

    /**
     * 分页查询某场考试的成绩
     *
     * @param page   分页参数
     * @param examId 考试ID
     * @param status 成绩状态（可为空）
     * @return 成绩分页数据
     */
    Page<ExamScore> pageByExam(Page<ExamScore> page, Long examId, String status);

    /**
     * 查询某位学生的全部考试成绩
     *
     * @param studentId 学生ID
     * @return 成绩列表
     */
    List<ExamScore> listByStudent(Long studentId);

    /**
     * 考试数据统计
     *
     * @param examId 考试ID
     * @return 统计结果：total（应考人数）、actual（实考人数）、absent（缺考人数）、
     *         passed（及格人数）、failed（不及格人数）
     */
    Map<String, Object> statByExam(Long examId);
}
