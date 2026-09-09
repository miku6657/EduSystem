package com.keshe.edumanage.controller.base;

import com.keshe.edumanage.common.result.Result;
import com.keshe.edumanage.entity.base.Major;
import com.keshe.edumanage.service.base.MajorService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 专业信息接口
 */
@RestController
@RequestMapping("/api/major")
@RequiredArgsConstructor
public class MajorController {

    private final MajorService majorService;

    /**
     * 查询全部专业
     */
    @GetMapping("/list")
    public Result<List<Major>> list() {
        return Result.success(majorService.list());
    }

    /**
     * 查询某系部下的专业
     */
    @GetMapping("/list-by-department/{departmentId}")
    public Result<List<Major>> listByDepartment(@PathVariable Long departmentId) {
        return Result.success(majorService.lambdaQuery()
                .eq(Major::getDepartmentId, departmentId)
                .list());
    }

    /**
     * 新增专业
     */
    @PostMapping
    public Result<Void> save(@RequestBody Major major) {
        majorService.save(major);
        return Result.success();
    }

    /**
     * 修改专业
     */
    @PutMapping
    public Result<Void> update(@RequestBody Major major) {
        majorService.updateById(major);
        return Result.success();
    }

    /**
     * 删除专业
     */
    @DeleteMapping("/{id}")
    public Result<Void> remove(@PathVariable Long id) {
        majorService.removeById(id);
        return Result.success();
    }
}
