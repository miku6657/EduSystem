package com.keshe.edumanage.dto;

import com.keshe.edumanage.entity.exam.ExamInfo;
import lombok.Data;

import java.util.List;

/**
 * 期末考试排考入参
 * <p>一次排考同时提交：考试信息 + 考场教室 + 监考教师</p>
 */
@Data
public class ExamArrangeDTO {

    /**
     * 考试信息（名称、课程、学期、类型、日期、起止时间）
     */
    private ExamInfo examInfo;

    /**
     * 考场教室ID列表（座位数自动取教室容量）
     */
    private List<Long> classroomIds;

    /**
     * 监考教师ID列表（第一位为主监考，其余为副监考）
     */
    private List<Long> monitorTeacherIds;
}
