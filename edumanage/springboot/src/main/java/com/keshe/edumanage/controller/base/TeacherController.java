package com.keshe.edumanage.controller.base;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.keshe.edumanage.common.result.Result;
import com.keshe.edumanage.entity.base.Teacher;
import com.keshe.edumanage.service.base.TeacherService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 教师信息接口
 */
@RestController
@RequestMapping("/api/teacher")
@RequiredArgsConstructor
public class TeacherController {

    private final TeacherService teacherService;

    /**
     * 分页条件查询教师（支持按姓名、类型、系部、教研室过滤）
     */
    @GetMapping("/page")
    public Result<Page<Teacher>> page(
            @RequestParam(defaultValue = "1") Integer pageNo,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) Long departmentId,
            @RequestParam(required = false) Long teachingGroupId) {
        Page<Teacher> page = teacherService.pageTeachers(
                new Page<>(pageNo, pageSize), name, type, departmentId, teachingGroupId);
        return Result.success(page);
    }

    /**
     * 根据ID查询教师
     */
    @GetMapping("/{id}")
    public Result<Teacher> getById(@PathVariable Long id) {
        return Result.success(teacherService.getById(id));
    }

    /**
     * 查询某系部下的教师
     */
    @GetMapping("/list-by-department/{departmentId}")
    public Result<List<Teacher>> listByDepartment(@PathVariable Long departmentId) {
        return Result.success(teacherService.listByDepartment(departmentId));
    }

    /**
     * 新增教师
     */
    @PostMapping
    public Result<Void> save(@RequestBody Teacher teacher) {
        teacherService.save(teacher);
        return Result.success();
    }

    /**
     * 修改教师
     */
    @PutMapping
    public Result<Void> update(@RequestBody Teacher teacher) {
        teacherService.updateById(teacher);
        return Result.success();
    }

    /**
     * 删除教师
     */
    @DeleteMapping("/{id}")
    public Result<Void> remove(@PathVariable Long id) {
        teacherService.removeById(id);
        return Result.success();
    }
}
