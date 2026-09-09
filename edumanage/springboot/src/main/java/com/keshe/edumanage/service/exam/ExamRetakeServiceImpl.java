package com.keshe.edumanage.service.exam;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.keshe.edumanage.common.exception.BusinessException;
import com.keshe.edumanage.entity.exam.ExamInfo;
import com.keshe.edumanage.entity.exam.ExamRetake;
import com.keshe.edumanage.entity.exam.ExamScore;
import com.keshe.edumanage.mapper.exam.ExamRetakeMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

/**
 * 补考重修业务实现
 */
@Service
@RequiredArgsConstructor
public class ExamRetakeServiceImpl extends ServiceImpl<ExamRetakeMapper, ExamRetake>
        implements ExamRetakeService {

    private final ExamInfoService examInfoService;
    private final ExamScoreService examScoreService;

    @Override
    public void applyRetake(Long studentId, Long courseId) {
        boolean exists = lambdaQuery()
                .eq(ExamRetake::getStudentId, studentId)
                .eq(ExamRetake::getCourseId, courseId)
                .exists();
        if (exists) {
            throw new BusinessException("该课程已有补考重修申请，请勿重复提交");
        }
        // 校验课程历史最高成绩，及格则不允许申请重修
        Double bestScore = getBestScore(studentId, courseId);
        if (bestScore == null) {
            throw new BusinessException("该课程暂无考试成绩记录");
        }
        if (bestScore >= examScoreService.PASS_SCORE) {
            throw new BusinessException("该课程成绩已及格，无需重修");
        }

        ExamRetake retake = new ExamRetake();
        retake.setStudentId(studentId);
        retake.setCourseId(courseId);
        retake.setType(TYPE_RETAKE);
        save(retake);
    }

    @Override
    public void assignExam(Long id, Long examId) {
        ExamRetake retake = getById(id);
        if (retake == null) {
            throw new BusinessException("补考重修记录不存在");
        }
        if (retake.getExamId() != null) {
            throw new BusinessException("该记录已安排考试，不能重复安排");
        }
        if (examInfoService.getById(examId) == null) {
            throw new BusinessException("考试信息不存在");
        }
        retake.setExamId(examId);
        updateById(retake);
    }

    @Override
    public List<ExamRetake> listByStudent(Long studentId) {
        return lambdaQuery()
                .eq(ExamRetake::getStudentId, studentId)
                .orderByDesc(ExamRetake::getId)
                .list();
    }

    @Override
    public List<ExamRetake> listByType(String type) {
        return lambdaQuery()
                .eq(ExamRetake::getType, type)
                .orderByDesc(ExamRetake::getId)
                .list();
    }

    /**
     * 查询学生在某门课程的历史最高分
     * <p>成绩表通过考试关联课程，先取该课程的全部考试，再查成绩取最高分</p>
     */
    private Double getBestScore(Long studentId, Long courseId) {
        List<Long> examIds = examInfoService.listByCourse(courseId)
                .stream()
                .map(ExamInfo::getId)
                .toList();
        if (examIds.isEmpty()) {
            return null;
        }
        return examScoreService.lambdaQuery()
                .in(ExamScore::getExamId, examIds)
                .eq(ExamScore::getStudentId, studentId)
                .list()
                .stream()
                .map(ExamScore::getScore)
                .filter(Objects::nonNull)
                .max(Double::compare)
                .orElse(null);
    }
}
