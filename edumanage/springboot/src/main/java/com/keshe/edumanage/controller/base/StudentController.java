package com.keshe.edumanage.controller.base;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.keshe.edumanage.common.result.Result;
import com.keshe.edumanage.entity.base.Student;
import com.keshe.edumanage.service.base.StudentService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 学生信息接口
 */
@RestController
@RequestMapping("/api/students")
@RequiredArgsConstructor
public class StudentController {

    private final StudentService studentService;

    /**
     * 分页条件查询学生
     * GET /api/students
     */
    @GetMapping
    public Result<Page<Student>> page(
            @RequestParam(defaultValue = "1") Integer pageNo,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long classId,
            @RequestParam(required = false) String status
    ) {
        Page<Student> page = studentService.pageStudents(
                new Page<>(pageNo, pageSize),
                keyword,
                classId,
                status
        );
        return Result.success(page);
    }

    /**
     * 查询学生详情
     * GET /api/students/{id}
     */
    @GetMapping("/{id}")
    public Result<Student> getById(
            @PathVariable Long id
    ) {
        return Result.success(
                studentService.getById(id)
        );
    }

    /**
     * 根据学号查询学生
     * GET /api/students?studentNo=xxx
     */
    @GetMapping(params = "studentNo")
    public Result<Student> getByStudentNo(
            @RequestParam String studentNo
    ) {
        return Result.success(
                studentService.getByStudentNo(studentNo)
        );
    }

    /**
     * 查询班级学生
     * GET /api/students?classId=xxx
     */
    @GetMapping(params = "classId")
    public Result<List<Student>> listByClass(
            @RequestParam Long classId
    ) {
        return Result.success(
                studentService.listByClass(classId)
        );
    }

    /**
     * 新增学生
     * POST /api/students
     */
    @PostMapping
    public Result<Void> save(
            @RequestBody Student student
    ) {
        studentService.save(student);
        return Result.success();
    }

    /**
     * 修改学生
     * PUT /api/students/{id}
     */
    @PutMapping("/{id}")
    public Result<Void> update(
            @PathVariable Long id,
            @RequestBody Student student
    ) {
        student.setId(id);
        studentService.updateById(student);
        return Result.success();
    }

    /**
     * 删除学生
     * DELETE /api/students/{id}
     */
    @DeleteMapping("/{id}")
    public Result<Void> remove(
            @PathVariable Long id
    ) {
        studentService.removeById(id);
        return Result.success();
    }
}
