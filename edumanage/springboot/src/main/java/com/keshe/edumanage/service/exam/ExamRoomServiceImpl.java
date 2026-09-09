package com.keshe.edumanage.service.exam;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.keshe.edumanage.entity.exam.ExamRoom;
import com.keshe.edumanage.mapper.exam.ExamRoomMapper;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 考场安排业务实现
 */
@Service
public class ExamRoomServiceImpl extends ServiceImpl<ExamRoomMapper, ExamRoom>
        implements ExamRoomService {

    @Override
    public List<ExamRoom> listByExam(Long examId) {
        return lambdaQuery()
                .eq(ExamRoom::getExamId, examId)
                .list();
    }

    @Override
    public void removeByExam(Long examId) {
        lambdaUpdate()
                .eq(ExamRoom::getExamId, examId)
                .remove();
    }
}
