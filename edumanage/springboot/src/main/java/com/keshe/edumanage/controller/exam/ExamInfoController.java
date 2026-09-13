package com.keshe.edumanage.controller.exam;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.keshe.edumanage.common.result.Result;
import com.keshe.edumanage.entity.exam.ExamInfo;
import com.keshe.edumanage.service.exam.ExamInfoService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/exams")
@RequiredArgsConstructor
public class ExamInfoController {


    private final ExamInfoService examInfoService;
    /**
     * 查询考试列表
     *
     * GET /api/exams
     */
    @GetMapping
    public Result<Page<ExamInfo>> page(

            @RequestParam(defaultValue = "1")
            Integer pageNo,

            @RequestParam(defaultValue = "10")
            Integer pageSize,

            @RequestParam(required = false)
            String name,

            @RequestParam(required = false)
            Long termId,

            @RequestParam(required = false)
            Long courseId,

            @RequestParam(required = false)
            String examType

    ){
        Page<ExamInfo> page =
                examInfoService.pageExams(
                        new Page<>(pageNo,pageSize),
                        name,
                        termId,
                        courseId,
                        examType
                );
        return Result.success(page);

    }
    /**
     * 查询考试详情
     *
     * GET /api/exams/{id}
     */
    @GetMapping("/{id}")
    public Result<ExamInfo> getById(

            @PathVariable Long id
    ){
        return Result.success(
                examInfoService.getById(id)
        );
    }
    /**
     * 新增考试
     *
     * POST /api/exams
     */
    @PostMapping
    public Result<Void> save(

            @RequestBody ExamInfo examInfo

    ){
        examInfoService.save(examInfo);
        return Result.success();

    }
    /**
     * 修改考试
     *
     * PUT /api/exams/{id}
     */
    @PutMapping("/{id}")
    public Result<Void> update(

            @PathVariable Long id,

            @RequestBody ExamInfo examInfo

    ){
        examInfo.setId(id);
        examInfoService.updateById(
                examInfo
        );
        return Result.success();

    }
    /**
     * 删除考试
     *
     * DELETE /api/exams/{id}
     */
    @DeleteMapping("/{id}")
    public Result<Void> remove(

            @PathVariable Long id

    ){

        examInfoService.removeExam(id);
        return Result.success();
    }

}
