package com.keshe.edumanage.controller.base;

import com.keshe.edumanage.common.result.Result;
import com.keshe.edumanage.entity.base.TeachingGroup;
import com.keshe.edumanage.service.base.TeachingGroupService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/teaching-groups")
@RequiredArgsConstructor
public class TeachingGroupController {

    private final TeachingGroupService teachingGroupService;

    /**
     * 查询教研室列表
     * GET /api/teaching-groups
     */
    @GetMapping
    public Result<List<TeachingGroup>> list(
            @RequestParam(required = false) Long departmentId
    ) {
        if (departmentId != null) {
            return Result.success(
                    teachingGroupService.lambdaQuery()
                            .eq(
                                    TeachingGroup::getDepartmentId,
                                    departmentId
                            )
                            .list()
            );
        }

        return Result.success(
                teachingGroupService.list()
        );
    }

    /**
     * 查询教研室详情
     * GET /api/teaching-groups/{id}
     */
    @GetMapping("/{id}")
    public Result<TeachingGroup> getById(
            @PathVariable Long id
    ) {
        return Result.success(
                teachingGroupService.getById(id)
        );
    }

    /**
     * 新增教研室
     * POST /api/teaching-groups
     */
    @PostMapping
    public Result<Void> save(
            @RequestBody TeachingGroup teachingGroup
    ) {
        teachingGroupService.save(teachingGroup);
        return Result.success();
    }

    /**
     * 修改教研室
     * PUT /api/teaching-groups/{id}
     */
    @PutMapping("/{id}")
    public Result<Void> update(
            @PathVariable Long id,
            @RequestBody TeachingGroup teachingGroup
    ) {
        teachingGroup.setId(id);
        teachingGroupService.updateById(teachingGroup);
        return Result.success();
    }

    /**
     * 删除教研室
     * DELETE /api/teaching-groups/{id}
     */
    @DeleteMapping("/{id}")
    public Result<Void> remove(
            @PathVariable Long id
    ) {
        teachingGroupService.removeById(id);
        return Result.success();
    }
}
