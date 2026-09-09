package com.keshe.edumanage.controller.base;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.keshe.edumanage.common.result.Result;
import com.keshe.edumanage.entity.base.Course;
import com.keshe.edumanage.service.base.CourseService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 课程信息接口
 */
@RestController
@RequestMapping("/api/course")
@RequiredArgsConstructor
public class CourseController {

    private final CourseService courseService;

    /**
     * 分页条件查询课程（支持按名称/代码关键字、类型、教研室过滤）
     */
    @GetMapping("/page")
    public Result<Page<Course>> page(
            @RequestParam(defaultValue = "1") Integer pageNo,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) Long teachingGroupId) {
        Page<Course> page = courseService.pageCourses(
                new Page<>(pageNo, pageSize), keyword, type, teachingGroupId);
        return Result.success(page);
    }

    /**
     * 根据ID查询课程
     */
    @GetMapping("/{id}")
    public Result<Course> getById(@PathVariable Long id) {
        return Result.success(courseService.getById(id));
    }

    /**
     * 根据课程代码查询课程
     */
    @GetMapping("/by-code/{courseCode}")
    public Result<Course> getByCourseCode(@PathVariable String courseCode) {
        return Result.success(courseService.getByCourseCode(courseCode));
    }

    /**
     * 查询某教研室的课程
     */
    @GetMapping("/list-by-teaching-group/{teachingGroupId}")
    public Result<List<Course>> listByTeachingGroup(@PathVariable Long teachingGroupId) {
        return Result.success(courseService.listByTeachingGroup(teachingGroupId));
    }

    /**
     * 新增课程
     */
    @PostMapping
    public Result<Void> save(@RequestBody Course course) {
        courseService.save(course);
        return Result.success();
    }

    /**
     * 修改课程
     */
    @PutMapping
    public Result<Void> update(@RequestBody Course course) {
        courseService.updateById(course);
        return Result.success();
    }

    /**
     * 删除课程
     */
    @DeleteMapping("/{id}")
    public Result<Void> remove(@PathVariable Long id) {
        courseService.removeById(id);
        return Result.success();
    }
}
