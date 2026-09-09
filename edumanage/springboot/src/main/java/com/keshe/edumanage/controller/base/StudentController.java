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
@RequestMapping("/api/student")
@RequiredArgsConstructor
public class StudentController {

    private final StudentService studentService;

    /**
     * 分页条件查询学生（支持按姓名/学号关键字、班级、学籍状态过滤）
     */
    @GetMapping("/page")
    public Result<Page<Student>> page(
            @RequestParam(defaultValue = "1") Integer pageNo,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long classId,
            @RequestParam(required = false) String status) {
        Page<Student> page = studentService.pageStudents(
                new Page<>(pageNo, pageSize), keyword, classId, status);
        return Result.success(page);
    }

    /**
     * 根据ID查询学生
     */
    @GetMapping("/{id}")
    public Result<Student> getById(@PathVariable Long id) {
        return Result.success(studentService.getById(id));
    }

    /**
     * 根据学号查询学生
     */
    @GetMapping("/by-no/{studentNo}")
    public Result<Student> getByStudentNo(@PathVariable String studentNo) {
        return Result.success(studentService.getByStudentNo(studentNo));
    }

    /**
     * 查询某班级下的全部学生
     */
    @GetMapping("/list-by-class/{classId}")
    public Result<List<Student>> listByClass(@PathVariable Long classId) {
        return Result.success(studentService.listByClass(classId));
    }

    /**
     * 新增学生
     */
    @PostMapping
    public Result<Void> save(@RequestBody Student student) {
        studentService.save(student);
        return Result.success();
    }

    /**
     * 修改学生
     */
    @PutMapping
    public Result<Void> update(@RequestBody Student student) {
        studentService.updateById(student);
        return Result.success();
    }

    /**
     * 删除学生
     */
    @DeleteMapping("/{id}")
    public Result<Void> remove(@PathVariable Long id) {
        studentService.removeById(id);
        return Result.success();
    }
}
