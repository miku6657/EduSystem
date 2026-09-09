package com.keshe.edumanage.controller.base;

import com.keshe.edumanage.common.result.Result;
import com.keshe.edumanage.entity.base.TeachingGroup;
import com.keshe.edumanage.service.base.TeachingGroupService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 教研室信息接口
 */
@RestController
@RequestMapping("/api/teaching-group")
@RequiredArgsConstructor
public class TeachingGroupController {

    private final TeachingGroupService teachingGroupService;

    /**
     * 查询全部教研室
     */
    @GetMapping("/list")
    public Result<List<TeachingGroup>> list() {
        return Result.success(teachingGroupService.list());
    }

    /**
     * 查询某系部下的教研室
     */
    @GetMapping("/list-by-department/{departmentId}")
    public Result<List<TeachingGroup>> listByDepartment(@PathVariable Long departmentId) {
        return Result.success(teachingGroupService.lambdaQuery()
                .eq(TeachingGroup::getDepartmentId, departmentId)
                .list());
    }

    /**
     * 新增教研室
     */
    @PostMapping
    public Result<Void> save(@RequestBody TeachingGroup teachingGroup) {
        teachingGroupService.save(teachingGroup);
        return Result.success();
    }

    /**
     * 修改教研室
     */
    @PutMapping
    public Result<Void> update(@RequestBody TeachingGroup teachingGroup) {
        teachingGroupService.updateById(teachingGroup);
        return Result.success();
    }

    /**
     * 删除教研室
     */
    @DeleteMapping("/{id}")
    public Result<Void> remove(@PathVariable Long id) {
        teachingGroupService.removeById(id);
        return Result.success();
    }
}
