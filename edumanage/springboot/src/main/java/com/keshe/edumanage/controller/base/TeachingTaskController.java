package com.keshe.edumanage.controller.base;

import com.keshe.edumanage.common.result.Result;
import com.keshe.edumanage.entity.base.TeachingTask;
import com.keshe.edumanage.service.base.TeachingTaskService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 教学任务（任课关系）接口
 * <p>供教务处维护「教师 - 课程 - 班级 - 学期」的任课关系</p>
 */
@RestController
@RequestMapping("/api/teaching-task")
@RequiredArgsConstructor
public class TeachingTaskController {

    private final TeachingTaskService teachingTaskService;

    /**
     * 查询某教师的全部教学任务
     */
    @GetMapping("/list-by-teacher/{teacherId}")
    public Result<List<TeachingTask>> listByTeacher(
            @PathVariable Long teacherId,
            @RequestParam(required = false) Long termId) {
        return Result.success(teachingTaskService.listByTeacher(teacherId, termId));
    }

    /**
     * 新增教学任务
     */
    @PostMapping
    public Result<Void> save(@RequestBody TeachingTask teachingTask) {
        teachingTaskService.save(teachingTask);
        return Result.success();
    }

    /**
     * 修改教学任务
     */
    @PutMapping
    public Result<Void> update(@RequestBody TeachingTask teachingTask) {
        teachingTaskService.updateById(teachingTask);
        return Result.success();
    }

    /**
     * 删除教学任务
     */
    @DeleteMapping("/{id}")
    public Result<Void> remove(@PathVariable Long id) {
        teachingTaskService.removeById(id);
        return Result.success();
    }
}
