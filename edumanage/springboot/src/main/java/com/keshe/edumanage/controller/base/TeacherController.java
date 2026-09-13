package com.keshe.edumanage.controller.base;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.keshe.edumanage.common.result.Result;
import com.keshe.edumanage.entity.base.ClassInfo;
import com.keshe.edumanage.entity.base.Course;
import com.keshe.edumanage.entity.base.Teacher;
import com.keshe.edumanage.service.base.TeacherService;
import com.keshe.edumanage.service.base.TeachingTaskService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 教师信息接口
 */
@RestController
@RequestMapping("/api/teachers")
@RequiredArgsConstructor
public class TeacherController {

    private final TeacherService teacherService;
    private final TeachingTaskService teachingTaskService;

    /**
     * 分页条件查询教师
     * GET /api/teachers
     */
    @GetMapping
    public Result<Page<Teacher>> page(
            @RequestParam(defaultValue = "1") Integer pageNo,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) Long departmentId,
            @RequestParam(required = false) Long teachingGroupId
    ) {
        Page<Teacher> page = teacherService.pageTeachers(
                new Page<>(pageNo, pageSize),
                name,
                type,
                departmentId,
                teachingGroupId
        );
        return Result.success(page);
    }

    /**
     * 查询教师详情
     * GET /api/teachers/{id}
     */
    @GetMapping("/{id}")
    public Result<Teacher> getById(
            @PathVariable Long id
    ) {
        return Result.success(
                teacherService.getById(id)
        );
    }

    /**
     * 查询某系部教师
     * GET /api/teachers?departmentId=xxx
     */
    @GetMapping(params = "departmentId")
    public Result<List<Teacher>> listByDepartment(
            @RequestParam Long departmentId
    ) {
        return Result.success(
                teacherService.listByDepartment(departmentId)
        );
    }

    /**
     * 根据工号查询教师（师生端登录名即工号，用于解析 teacherId）
     * GET /api/teachers?teacherNo=T001
     */
    @GetMapping(params = "teacherNo")
    public Result<Teacher> getByTeacherNo(
            @RequestParam String teacherNo
    ) {
        return Result.success(
                teacherService.getByTeacherNo(teacherNo)
        );
    }

    /**
     * 教师任教的班级（师生端「我的班级」，用于考勤点名等场景）
     * GET /api/teachers/{id}/classes
     */
    @GetMapping("/{id}/classes")
    public Result<List<ClassInfo>> myClasses(
            @PathVariable Long id,
            @RequestParam(required = false) Long termId
    ) {
        return Result.success(
                teachingTaskService.listMyClasses(id, termId)
        );
    }

    /**
     * 教师任教的课程（师生端「我的课程」，用于教学日志、考核方式申报等场景）
     * GET /api/teachers/{id}/courses
     */
    @GetMapping("/{id}/courses")
    public Result<List<Course>> myCourses(
            @PathVariable Long id,
            @RequestParam(required = false) Long termId
    ) {
        return Result.success(
                teachingTaskService.listMyCourses(id, termId)
        );
    }

    /**
     * 新增教师
     * POST /api/teachers
     */
    @PostMapping
    public Result<Void> save(
            @RequestBody Teacher teacher
    ) {
        teacherService.save(teacher);
        return Result.success();
    }

    /**
     * 修改教师
     * PUT /api/teachers/{id}
     */
    @PutMapping("/{id}")
    public Result<Void> update(
            @PathVariable Long id,
            @RequestBody Teacher teacher
    ) {
        teacher.setId(id);
        teacherService.updateById(teacher);
        return Result.success();
    }

    /**
     * 删除教师
     * DELETE /api/teachers/{id}
     */
    @DeleteMapping("/{id}")
    public Result<Void> remove(
            @PathVariable Long id
    ) {
        teacherService.removeById(id);
        return Result.success();
    }
}
