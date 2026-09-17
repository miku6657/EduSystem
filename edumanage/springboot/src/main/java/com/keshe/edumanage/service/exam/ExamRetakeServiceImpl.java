package com.keshe.edumanage.service.exam;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.keshe.edumanage.common.exception.BusinessException;
import com.keshe.edumanage.dto.ExamArrangeDTO;
import com.keshe.edumanage.entity.base.Course;
import com.keshe.edumanage.entity.exam.ExamInfo;
import com.keshe.edumanage.entity.exam.ExamRetake;
import com.keshe.edumanage.entity.exam.ExamScore;
import com.keshe.edumanage.mapper.exam.ExamRetakeMapper;
import com.keshe.edumanage.service.base.CourseService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class ExamRetakeServiceImpl
        extends ServiceImpl<
        ExamRetakeMapper,
        ExamRetake
        >
        implements ExamRetakeService {

    private final ExamInfoService
            examInfoService;

    private final ExamScoreService
            examScoreService;

    private final CourseService
            courseService;

    /**
     * 学生申请重修。
     */
    @Override
    public void applyRetake(
            Long studentId,
            Long courseId
    ) {

        /*
         * 只限制“正在处理”的申请。
         *
         * COMPLETED / REJECTED
         * 不阻止学生以后重新申请。
         */
        boolean exists =
                lambdaQuery()
                        .eq(
                                ExamRetake::getStudentId,
                                studentId
                        )
                        .eq(
                                ExamRetake::getCourseId,
                                courseId
                        )
                        .in(
                                ExamRetake::getStatus,
                                STATUS_WAIT,
                                STATUS_APPROVED,
                                STATUS_ARRANGED
                        )
                        .exists();

        if (exists) {
            throw new BusinessException(
                    "该课程已有待处理的重修申请"
            );
        }

        /*
         * 必须真的挂科。
         */
        Double bestScore =
                getBestScore(
                        studentId,
                        courseId
                );

        if (bestScore == null) {
            throw new BusinessException(
                    "该课程暂无成绩记录"
            );
        }

        if (
                bestScore
                        >= ExamScoreService.PASS_SCORE
        ) {
            throw new BusinessException(
                    "该课程成绩已及格，无需申请重修"
            );
        }

        ExamRetake retake =
                new ExamRetake();

        retake.setStudentId(
                studentId
        );

        retake.setCourseId(
                courseId
        );

        retake.setType(
                TYPE_RETAKE
        );

        retake.setStatus(
                STATUS_WAIT
        );

        save(retake);
    }

    /**
     * 管理员批准。
     */
    @Override
    public void approve(
            Long id
    ) {

        ExamRetake retake =
                getRequired(id);

        if (
                !STATUS_WAIT.equals(
                        retake.getStatus()
                )
        ) {
            throw new BusinessException(
                    "当前申请状态不能执行批准操作"
            );
        }

        retake.setStatus(
                STATUS_APPROVED
        );

        updateById(retake);
    }

    /**
     * 管理员驳回。
     */
    @Override
    public void reject(
            Long id
    ) {

        ExamRetake retake =
                getRequired(id);

        if (
                !STATUS_WAIT.equals(
                        retake.getStatus()
                )
        ) {
            throw new BusinessException(
                    "当前申请状态不能驳回"
            );
        }

        retake.setStatus(
                STATUS_REJECTED
        );

        updateById(retake);
    }

    /**
     * 安排考试。
     */
    @Override
    public void assignExam(
            Long id,
            Long examId
    ) {

        ExamRetake retake =
                getRequired(id);

        if (
                !STATUS_APPROVED.equals(
                        retake.getStatus()
                )
        ) {
            throw new BusinessException(
                    "只有已批准的申请才能安排考试"
            );
        }

        ExamInfo exam =
                examInfoService.getById(
                        examId
                );

        if (exam == null) {
            throw new BusinessException(
                    "考试信息不存在"
            );
        }

        /*
         * 防止给 Java 挂科学生
         * 安排成 MYSQL 考试。
         */
        if (
                !Objects.equals(
                        exam.getCourseId(),
                        retake.getCourseId()
                )
        ) {
            throw new BusinessException(
                    "安排的考试与申请课程不一致"
            );
        }

        retake.setExamId(
                examId
        );

        retake.setStatus(
                STATUS_ARRANGED
        );

        updateById(retake);
    }

    /**
     * 为已批准的重修申请创建专门的重修考试。
     *
     * APPROVED -> ARRANGED
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void arrangeRetakeExam(
            Long id,
            ExamArrangeDTO dto
    ) {

        ExamRetake retake =
                getRequired(id);

        /*
         * 只有审批通过以后才能安排考试。
         */
        if (
            !STATUS_APPROVED.equals(
                retake.getStatus()
            )
        ) {
            throw new BusinessException(
                "只有已批准的重修申请才能安排考试"
            );
        }

        if (retake.getExamId() != null) {
            throw new BusinessException(
                "该重修申请已经安排考试"
            );
        }

        if (
            dto == null
            || dto.getExamInfo() == null
        ) {
            throw new BusinessException(
                "考试信息不能为空"
            );
        }

        if (
            dto.getClassroomIds() == null
            || dto.getClassroomIds().isEmpty()
        ) {
            throw new BusinessException(
                "请选择考试教室"
            );
        }

        if (
            dto.getMonitorTeacherIds() == null
            || dto.getMonitorTeacherIds().isEmpty()
        ) {
            throw new BusinessException(
                "请选择监考教师"
            );
        }

        Course course =
                courseService.getById(
                    retake.getCourseId()
                );

        if (course == null) {
            throw new BusinessException(
                "申请对应的课程不存在"
            );
        }

        ExamInfo exam =
                dto.getExamInfo();

        if (exam.getExamDate() == null) {
            throw new BusinessException(
                "请选择考试日期"
            );
        }

        if (
            exam.getStartTime() == null
            || exam.getEndTime() == null
        ) {
            throw new BusinessException(
                "请选择考试时间"
            );
        }

        if (
            !exam.getStartTime()
                .isBefore(
                    exam.getEndTime()
                )
        ) {
            throw new BusinessException(
                "考试结束时间必须晚于开始时间"
            );
        }

        /*
         * termId前端没传时，
         * 优先使用课程所属学期。
         */
        if (exam.getTermId() == null) {
            exam.setTermId(
                course.getTermId()
            );
        }

        if (exam.getTermId() == null) {
            throw new BusinessException(
                "无法确定考试所属学期"
            );
        }

        /*
         * 以下字段全部由后端固定。
         *
         * 前端不能把数据结构重修考试
         * 改成Java考试。
         */
        exam.setId(null);

        exam.setCourseId(
            retake.getCourseId()
        );

        exam.setName(
            course.getName()
            + "重修考试"
        );

        exam.setExamType(
            "重修考试"
        );

        /*
         * 调用原来的排考能力。
         *
         * 这里会：
         *
         * 1. 检查教室时间冲突
         * 2. 检查监考教师时间冲突
         * 3. INSERT exam_info
         * 4. INSERT exam_room
         * 5. INSERT exam_monitor
         *
         * save之后exam.id会自动回填。
         */
        examInfoService.arrangeExam(
            exam,
            dto.getClassroomIds(),
            dto.getMonitorTeacherIds()
        );

        /*
         * 将刚刚创建的新考试绑定给重修申请。
         */
        retake.setExamId(
            exam.getId()
        );

        retake.setStatus(
            STATUS_ARRANGED
        );

        updateById(retake);
    }

    @Override
    public List<ExamRetake>
    listByStudent(
            Long studentId
    ) {

        return lambdaQuery()
                .eq(
                        ExamRetake::getStudentId,
                        studentId
                )
                .orderByDesc(
                        ExamRetake::getId
                )
                .list();
    }

    @Override
    public List<ExamRetake>
    listByType(
            String type
    ) {

        return lambdaQuery()
                .eq(
                        ExamRetake::getType,
                        type
                )
                .orderByDesc(
                        ExamRetake::getId
                )
                .list();
    }

    @Override
    public List<ExamRetake>
    listByExam(
            Long examId
    ) {

        return lambdaQuery()
                .eq(
                        ExamRetake::getExamId,
                        examId
                )
                .eq(
                        ExamRetake::getStatus,
                        STATUS_ARRANGED
                )
                .list();
    }

    /**
     * 教师录入重修成绩。
     *
     * 不覆盖原期末考试成绩，
     * 而是为本次重修考试新增一条成绩记录。
     *
     * 这样同一学生、同一课程可以同时存在：
     *
     * 原期末考试   50
     * 重修考试     60
     */
    @Override
    @Transactional(
            rollbackFor = Exception.class
    )
    public void saveRetakeScores(
            Long examId,
            List<ExamScore> scores
    ) {

        ExamInfo retakeExam =
                examInfoService.getById(
                        examId
                );

        if (retakeExam == null) {
            throw new BusinessException(
                    "重修考试不存在"
            );
        }

        /*
         * 这场考试真正允许参加的学生。
         */
        List<ExamRetake> retakes =
                lambdaQuery()
                        .eq(
                                ExamRetake::getExamId,
                                examId
                        )
                        .eq(
                                ExamRetake::getStatus,
                                STATUS_ARRANGED
                        )
                        .list();

        if (retakes.isEmpty()) {
            throw new BusinessException(
                    "该考试没有待录入的重修学生"
            );
        }

        for (
                ExamScore newScore
                : scores
        ) {

            /*
             * 第一层关键校验：
             *
             * 教师传来的学生必须真的
             * 参加这场重修考试。
             */
            ExamRetake retake =
                    retakes.stream()
                            .filter(
                                    item ->
                                            Objects.equals(
                                                    item.getStudentId(),
                                                    newScore.getStudentId()
                                            )
                            )
                            .findFirst()
                            .orElseThrow(
                                    () ->
                                            new BusinessException(
                                                    "该学生未参加本场重修考试"
                                            )
                            );

            /*
             * 查询这个学生在当前重修考试
             * 是否已经录过成绩。
             *
             * 避免教师重复保存产生两条。
             */
            ExamScore score =
                    examScoreService
                            .lambdaQuery()
                            .eq(
                                    ExamScore::getExamId,
                                    examId
                            )
                            .eq(
                                    ExamScore::getStudentId,
                                    newScore.getStudentId()
                            )
                            .one();

            if (score == null) {

                /*
                 * 第一次录入：
                 * INSERT
                 */
                score =
                        new ExamScore();

                score.setExamId(
                        examId
                );

                score.setStudentId(
                        newScore.getStudentId()
                );

                score.setScore(
                        newScore.getScore()
                );

                score.setStatus(
                        newScore.getStatus()
                );

                examScoreService.save(
                        score
                );

            } else {

                /*
                 * 教师修改当前重修成绩：
                 * UPDATE当前重修记录。
                 *
                 * 不碰原期末考试成绩。
                 */
                score.setScore(
                        newScore.getScore()
                );

                score.setStatus(
                        newScore.getStatus()
                );

                examScoreService.updateById(
                        score
                );
            }

            /*
             * 当前学生重修完成。
             */
            retake.setStatus(
                    STATUS_COMPLETED
            );

            updateById(
                    retake
            );
        }
    }

    private ExamRetake getRequired(
            Long id
    ) {

        ExamRetake retake =
                getById(id);

        if (retake == null) {
            throw new BusinessException(
                    "补考重修申请不存在"
            );
        }

        return retake;
    }

    /**
     * 查询该学生这门课程历史最高分。
     */
    private Double getBestScore(
            Long studentId,
            Long courseId
    ) {

        List<Long> examIds =
                examInfoService
                        .listByCourse(
                                courseId
                        )
                        .stream()
                        .map(
                                ExamInfo::getId
                        )
                        .toList();

        if (examIds.isEmpty()) {
            return null;
        }

        return examScoreService
                .lambdaQuery()
                .in(
                        ExamScore::getExamId,
                        examIds
                )
                .eq(
                        ExamScore::getStudentId,
                        studentId
                )
                .list()
                .stream()
                .map(
                        ExamScore::getScore
                )
                .filter(
                        Objects::nonNull
                )
                .max(
                        Double::compare
                )
                .orElse(null);
    }
}
