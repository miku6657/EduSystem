package com.keshe.edumanage.controller.exam;

import com.keshe.edumanage.common.result.Result;
import com.keshe.edumanage.entity.exam.ExamRoom;
import com.keshe.edumanage.service.exam.ExamRoomService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 考场安排接口
 */
@RestController
@RequestMapping("/api/exam-room")
@RequiredArgsConstructor
public class ExamRoomController {

    private final ExamRoomService examRoomService;

    /**
     * 查询某场考试的考场安排
     */
    @GetMapping("/list-by-exam/{examId}")
    public Result<List<ExamRoom>> listByExam(@PathVariable Long examId) {
        return Result.success(examRoomService.listByExam(examId));
    }
}
