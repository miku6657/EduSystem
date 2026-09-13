package com.keshe.edumanage.controller.base;

import com.keshe.edumanage.common.result.Result;
import com.keshe.edumanage.entity.base.TeachingTask;
import com.keshe.edumanage.service.base.TeachingTaskService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 教学任务（任课关系）接口
 * <p>供教务处维护「教师 - 课程 - 班级 - 学期」的任课关系；
 * 路径风格与 base 模块其余接口保持一致（复数 + query 过滤）。</p>
 */
@RestController
@RequestMapping("/api/teaching-tasks")
@RequiredArgsConstructor
public class TeachingTaskController {

    private final TeachingTaskService teachingTaskService;

    /**
     * 按教师查询教学任务
     * GET /api/teaching-tasks?teacherId=1&termId=2
     */
    @GetMapping(params = "teacherId")
    public Result<List<TeachingTask>> listByTeacher(
            @RequestParam Long teacherId,
            @RequestParam(required = false) Long termId
    ) {
        return Result.success(
                teachingTaskService.listByTeacher(teacherId, termId)
        );
    }

    /**
     * 按班级查询教学任务（师生端「我的课表」）
     * GET /api/teaching-tasks?classId=1&termId=2
     */
    @GetMapping(params = "classId")
    public Result<List<TeachingTask>> listByClass(
            @RequestParam Long classId,
            @RequestParam(required = false) Long termId
    ) {
        return Result.success(
                teachingTaskService.listByClass(classId, termId)
        );
    }

    /**
     * 查询教学任务详情
     * GET /api/teaching-tasks/{id}
     */
    @GetMapping("/{id}")
    public Result<TeachingTask> getById(
            @PathVariable Long id
    ) {
        return Result.success(
                teachingTaskService.getById(id)
        );
    }

    /**
     * 新增教学任务
     * POST /api/teaching-tasks
     */
    @PostMapping
    public Result<Void> save(
            @RequestBody TeachingTask teachingTask
    ) {
        teachingTaskService.save(teachingTask);
        return Result.success();
    }

    /**
     * 修改教学任务
     * PUT /api/teaching-tasks/{id}
     */
    @PutMapping("/{id}")
    public Result<Void> update(
            @PathVariable Long id,
            @RequestBody TeachingTask teachingTask
    ) {
        teachingTask.setId(id);
        teachingTaskService.updateById(teachingTask);
        return Result.success();
    }

    /**
     * 删除教学任务
     * DELETE /api/teaching-tasks/{id}
     */
    @DeleteMapping("/{id}")
    public Result<Void> remove(
            @PathVariable Long id
    ) {
        teachingTaskService.removeById(id);
        return Result.success();
    }
}
