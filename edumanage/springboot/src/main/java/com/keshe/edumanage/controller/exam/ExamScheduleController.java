package com.keshe.edumanage.controller.exam;

import com.keshe.edumanage.common.result.Result;
import com.keshe.edumanage.dto.ExamArrangeDTO;
import com.keshe.edumanage.service.exam.ExamInfoService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/exam-schedules")
@RequiredArgsConstructor
public class ExamScheduleController {

    private final ExamInfoService examInfoService;


    /**
     * 考试排考
     *
     * POST /api/exam-schedules
     */
    @PostMapping
    public Result<Void> arrange(
            @RequestBody ExamArrangeDTO dto
    ) {

        examInfoService.arrangeExam(
                dto.getExamInfo(),
                dto.getClassroomIds(),
                dto.getMonitorTeacherIds()
        );

        return Result.success();
    }
}