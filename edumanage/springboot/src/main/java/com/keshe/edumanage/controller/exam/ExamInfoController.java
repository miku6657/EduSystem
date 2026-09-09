package com.keshe.edumanage.controller.exam;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.keshe.edumanage.common.result.Result;
import com.keshe.edumanage.dto.ExamArrangeDTO;
import com.keshe.edumanage.entity.exam.ExamInfo;
import com.keshe.edumanage.service.exam.ExamInfoService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 考试信息接口
 * <p>期末考试安排（自动排考场 + 冲突判断）、考试信息查询</p>
 */
@RestController
@RequestMapping("/api/exam")
@RequiredArgsConstructor
public class ExamInfoController {

    private final ExamInfoService examInfoService;

    /**
     * 期末考试排考（考试 + 考场 + 监考一次提交，含冲突判断）
     */
    @PostMapping("/arrange")
    public Result<Void> arrange(@RequestBody ExamArrangeDTO dto) {
        examInfoService.arrangeExam(dto.getExamInfo(), dto.getClassroomIds(),
                dto.getMonitorTeacherIds());
        return Result.success();
    }

    /**
     * 分页条件查询考试（面向师生的考试信息查询）
     */
    @GetMapping("/page")
    public Result<Page<ExamInfo>> page(
            @RequestParam(defaultValue = "1") Integer pageNo,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Long termId,
            @RequestParam(required = false) String examType) {
        Page<ExamInfo> page = examInfoService.pageExams(
                new Page<>(pageNo, pageSize), name, termId, examType);
        return Result.success(page);
    }

    /**
     * 根据ID查询考试
     */
    @GetMapping("/{id}")
    public Result<ExamInfo> getById(@PathVariable Long id) {
        return Result.success(examInfoService.getById(id));
    }

    /**
     * 查询某学期的考试列表
     */
    @GetMapping("/list-by-term/{termId}")
    public Result<List<ExamInfo>> listByTerm(@PathVariable Long termId) {
        return Result.success(examInfoService.listByTerm(termId));
    }

    /**
     * 查询某门课程的考试列表
     */
    @GetMapping("/list-by-course/{courseId}")
    public Result<List<ExamInfo>> listByCourse(@PathVariable Long courseId) {
        return Result.success(examInfoService.listByCourse(courseId));
    }

    /**
     * 修改考试基本信息
     */
    @PutMapping
    public Result<Void> update(@RequestBody ExamInfo examInfo) {
        examInfoService.updateById(examInfo);
        return Result.success();
    }

    /**
     * 删除考试（连同考场安排、监考安排一并删除）
     */
    @DeleteMapping("/{id}")
    public Result<Void> remove(@PathVariable Long id) {
        examInfoService.removeExam(id);
        return Result.success();
    }
}
