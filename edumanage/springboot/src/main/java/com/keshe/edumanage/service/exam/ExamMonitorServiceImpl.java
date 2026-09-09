package com.keshe.edumanage.service.exam;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.keshe.edumanage.entity.exam.ExamMonitor;
import com.keshe.edumanage.mapper.exam.ExamMonitorMapper;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 监考安排业务实现
 */
@Service
public class ExamMonitorServiceImpl extends ServiceImpl<ExamMonitorMapper, ExamMonitor>
        implements ExamMonitorService {

    @Override
    public List<ExamMonitor> listByExam(Long examId) {
        return lambdaQuery()
                .eq(ExamMonitor::getExamId, examId)
                .list();
    }

    @Override
    public List<ExamMonitor> listByTeacher(Long teacherId) {
        return lambdaQuery()
                .eq(ExamMonitor::getTeacherId, teacherId)
                .list();
    }

    @Override
    public void removeByExam(Long examId) {
        lambdaUpdate()
                .eq(ExamMonitor::getExamId, examId)
                .remove();
    }
}
