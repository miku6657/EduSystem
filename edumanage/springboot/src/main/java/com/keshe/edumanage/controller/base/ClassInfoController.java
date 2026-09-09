package com.keshe.edumanage.controller.base;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.keshe.edumanage.common.result.Result;
import com.keshe.edumanage.entity.base.ClassInfo;
import com.keshe.edumanage.service.base.ClassInfoService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 班级信息接口
 */
@RestController
@RequestMapping("/api/class")
@RequiredArgsConstructor
public class ClassInfoController {

    private final ClassInfoService classInfoService;

    /**
     * 分页条件查询班级（支持按名称、专业、校区、年级过滤）
     */
    @GetMapping("/page")
    public Result<Page<ClassInfo>> page(
            @RequestParam(defaultValue = "1") Integer pageNo,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Long majorId,
            @RequestParam(required = false) Long campusId,
            @RequestParam(required = false) String grade) {
        Page<ClassInfo> page = classInfoService.pageClasses(
                new Page<>(pageNo, pageSize), name, majorId, campusId, grade);
        return Result.success(page);
    }

    /**
     * 根据ID查询班级
     */
    @GetMapping("/{id}")
    public Result<ClassInfo> getById(@PathVariable Long id) {
        return Result.success(classInfoService.getById(id));
    }

    /**
     * 查询某专业下的班级
     */
    @GetMapping("/list-by-major/{majorId}")
    public Result<List<ClassInfo>> listByMajor(@PathVariable Long majorId) {
        return Result.success(classInfoService.listByMajor(majorId));
    }

    /**
     * 新增班级
     */
    @PostMapping
    public Result<Void> save(@RequestBody ClassInfo classInfo) {
        classInfoService.save(classInfo);
        return Result.success();
    }

    /**
     * 修改班级
     */
    @PutMapping
    public Result<Void> update(@RequestBody ClassInfo classInfo) {
        classInfoService.updateById(classInfo);
        return Result.success();
    }

    /**
     * 删除班级
     */
    @DeleteMapping("/{id}")
    public Result<Void> remove(@PathVariable Long id) {
        classInfoService.removeById(id);
        return Result.success();
    }
}
