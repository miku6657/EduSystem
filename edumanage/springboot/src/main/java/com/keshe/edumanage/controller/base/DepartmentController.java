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
@RequestMapping("/api/departments")
@RequiredArgsConstructor
public class DepartmentController {

    private final DepartmentService departmentService;

    /**
     * 查询系部列表
     * GET /api/departments
     */
    @GetMapping
    public Result<List<Department>> list() {
        return Result.success(
                departmentService.list()
        );
    }

    /**
     * 查询系部详情
     * GET /api/departments/{id}
     */
    @GetMapping("/{id}")
    public Result<Department> getById(
            @PathVariable Long id
    ) {
        return Result.success(
                departmentService.getById(id)
        );
    }

    /**
     * 新增系部
     * POST /api/departments
     */
    @PostMapping
    public Result<Void> save(
            @RequestBody Department department
    ) {
        departmentService.save(department);
        return Result.success();
    }

    /**
     * 修改系部
     * PUT /api/departments/{id}
     */
    @PutMapping("/{id}")
    public Result<Void> update(
            @PathVariable Long id,
            @RequestBody Department department
    ) {
        department.setId(id);
        departmentService.updateById(department);
        return Result.success();
    }

    /**
     * 删除系部
     * DELETE /api/departments/{id}
     */
    @DeleteMapping("/{id}")
    public Result<Void> remove(
            @PathVariable Long id
    ) {
        departmentService.removeById(id);
        return Result.success();
    }
}
