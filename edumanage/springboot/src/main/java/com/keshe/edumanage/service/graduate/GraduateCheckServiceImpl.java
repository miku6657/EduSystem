package com.keshe.edumanage.service.graduate;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.keshe.edumanage.common.exception.BusinessException;
import com.keshe.edumanage.entity.exam.ExamInfo;
import com.keshe.edumanage.entity.exam.ExamScore;
import com.keshe.edumanage.entity.graduate.GraduateCheck;
import com.keshe.edumanage.mapper.graduate.GraduateCheckMapper;
import com.keshe.edumanage.service.exam.ExamInfoService;
import com.keshe.edumanage.service.exam.ExamScoreService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 毕业资格审核业务实现
 */
@Service
public class GraduateCheckServiceImpl extends ServiceImpl<GraduateCheckMapper, GraduateCheck>
        implements GraduateCheckService {

    private final ExamScoreService examScoreService;

    private final ExamInfoService examInfoService;

    public GraduateCheckServiceImpl(
            ExamScoreService examScoreService,
            ExamInfoService examInfoService
    ) {
        this.examScoreService = examScoreService;
        this.examInfoService = examInfoService;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void audit(GraduateCheck check) {
        if (check.getStudentId() == null) {
            throw new BusinessException("学生信息不能为空");
        }

        boolean eligible = isGraduationEligible(check.getStudentId());

        String result = eligible ? STATUS_PASS : STATUS_FAIL;

        check.setCheckStatus(result);
        check.setCourseStatus(result);
        check.setCreditStatus(result);

        if (!eligible) {
            check.setRemark("存在课程最高成绩低于60分，毕业审核不通过");
        } else if (check.getRemark() == null || check.getRemark().isBlank()) {
            check.setRemark("所有课程最高成绩均不低于60分，毕业审核通过");
        }

        GraduateCheck exist = getByStudent(check.getStudentId());

        if (exist != null) {
            check.setId(exist.getId());
        }

        saveOrUpdate(check);
    }

    @Override
    public List<GraduateCheck> listByStatus(String checkStatus) {
        return lambdaQuery()
                .eq(checkStatus != null && !checkStatus.isBlank(),
                        GraduateCheck::getCheckStatus, checkStatus)
                .orderByDesc(GraduateCheck::getId)
                .list();
    }

    @Override
    public GraduateCheck getByStudent(Long studentId) {
        return lambdaQuery()
                .eq(GraduateCheck::getStudentId, studentId)
                .one();
    }

    /**
     * 判断学生是否满足毕业条件。
     *
     * 按课程取最高分：
     * 同一课程可能存在原期末成绩和重修成绩
     * 两条记录，只取最高的一条参与判断。
     *
     * 只要还有一门课程最高分低于 60，
     * 就不能毕业。
     */
    private boolean isGraduationEligible(
            Long studentId
    ) {

        /*
         * 1. 查询该学生全部成绩。
         */
        List<ExamScore> scores =
                examScoreService
                        .listByStudent(
                                studentId
                        );

        /*
         * 没有有效成绩时，
         * 沿用当前系统原来的规则。
         */
        if (scores.isEmpty()) {
            return true;
        }

        /*
         * 2. 拿到所有考试ID。
         */
        List<Long> examIds =
                scores.stream()
                        .map(
                                ExamScore::getExamId
                        )
                        .filter(
                                Objects::nonNull
                        )
                        .distinct()
                        .toList();

        if (examIds.isEmpty()) {
            return true;
        }

        /*
         * 3. 查询考试，
         * 得到：
         *
         * examId -> courseId
         */
        Map<Long, Long> examCourseMap =
                examInfoService
                        .listByIds(examIds)
                        .stream()
                        .filter(
                                exam ->
                                        exam.getCourseId()
                                                != null
                        )
                        .collect(
                                Collectors.toMap(
                                        ExamInfo::getId,
                                        ExamInfo::getCourseId,
                                        (a, b) -> a
                                )
                        );

        /*
         * 4.
         * courseId -> 该课程最高成绩
         */
        Map<Long, Double> bestScoreMap =
                new HashMap<>();

        for (
                ExamScore score
                : scores
        ) {

            if (
                    score.getScore() == null
                            || score.getExamId() == null
            ) {
                continue;
            }

            Long courseId =
                    examCourseMap.get(
                            score.getExamId()
                    );

            if (courseId == null) {
                continue;
            }

            bestScoreMap.merge(
                    courseId,
                    score.getScore(),
                    Math::max
            );
        }

        /*
         * 5.
         * 每门课程最高成绩
         * 只要还有 <60，
         * 就不能毕业。
         */
        return bestScoreMap
                .values()
                .stream()
                .noneMatch(
                        score ->
                                score < 60
                );
    }
}
