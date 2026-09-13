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
@RequestMapping("/api/courses")
@RequiredArgsConstructor
public class CourseController {

    private final CourseService courseService;

    /**
     * 查询课程列表
     * GET /api/courses
     */
    @GetMapping
    public Result<Page<Course>> page(
            @RequestParam(defaultValue = "1") Integer pageNo,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) Long teachingGroupId,
            @RequestParam(required = false) String courseCode
    ) {
        Page<Course> page = courseService.pageCourses(
                new Page<>(pageNo, pageSize),
                keyword,
                type,
                teachingGroupId
        );
        return Result.success(page);
    }

    /**
     * 查询课程详情
     * GET /api/courses/{id}
     */
    @GetMapping("/{id}")
    public Result<Course> getById(
            @PathVariable Long id
    ) {
        return Result.success(
                courseService.getById(id)
        );
    }

    /**
     * 根据课程代码查询
     * GET /api/courses?courseCode=xxx
     */
    @GetMapping(params = "courseCode")
    public Result<Course> getByCourseCode(
            @RequestParam String courseCode
    ) {
        return Result.success(
                courseService.getByCourseCode(courseCode)
        );
    }

    /**
     * 查询教研室课程
     * GET /api/courses?teachingGroupId=xxx
     */
    @GetMapping(params = "teachingGroupId")
    public Result<List<Course>> listByTeachingGroup(
            @RequestParam Long teachingGroupId
    ) {
        return Result.success(
                courseService.listByTeachingGroup(teachingGroupId)
        );
    }

    /**
     * 新增课程
     * POST /api/courses
     */
    @PostMapping
    public Result<Void> save(
            @RequestBody Course course
    ) {
        courseService.save(course);
        return Result.success();
    }

    /**
     * 修改课程
     * PUT /api/courses/{id}
     */
    @PutMapping("/{id}")
    public Result<Void> update(
            @PathVariable Long id,
            @RequestBody Course course
    ) {
        course.setId(id);
        courseService.updateById(course);
        return Result.success();
    }

    /**
     * 删除课程
     * DELETE /api/courses/{id}
     */
    @DeleteMapping("/{id}")
    public Result<Void> remove(
            @PathVariable Long id
    ) {
        courseService.removeById(id);
        return Result.success();
    }
}