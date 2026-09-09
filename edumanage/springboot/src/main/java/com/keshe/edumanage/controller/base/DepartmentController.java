package com.keshe.edumanage.controller.base;

import com.keshe.edumanage.common.result.Result;
import com.keshe.edumanage.entity.base.Department;
import com.keshe.edumanage.service.base.DepartmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 系部信息接口
 */
@RestController
@RequestMapping("/api/department")
@RequiredArgsConstructor
public class DepartmentController {

    private final DepartmentService departmentService;

    /**
     * 查询全部系部
     */
    @GetMapping("/list")
    public Result<List<Department>> list() {
        return Result.success(departmentService.list());
    }

    /**
     * 根据ID查询系部
     */
    @GetMapping("/{id}")
    public Result<Department> getById(@PathVariable Long id) {
        return Result.success(departmentService.getById(id));
    }

    /**
     * 新增系部
     */
    @PostMapping
    public Result<Void> save(@RequestBody Department department) {
        departmentService.save(department);
        return Result.success();
    }

    /**
     * 修改系部
     */
    @PutMapping
    public Result<Void> update(@RequestBody Department department) {
        departmentService.updateById(department);
        return Result.success();
    }

    /**
     * 删除系部
     */
    @DeleteMapping("/{id}")
    public Result<Void> remove(@PathVariable Long id) {
        departmentService.removeById(id);
        return Result.success();
    }
}
