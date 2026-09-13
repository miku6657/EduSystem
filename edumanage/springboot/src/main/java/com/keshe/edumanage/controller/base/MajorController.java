package com.keshe.edumanage.controller.base;

import com.keshe.edumanage.common.result.Result;
import com.keshe.edumanage.entity.base.Major;
import com.keshe.edumanage.service.base.MajorService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/majors")
@RequiredArgsConstructor
public class MajorController {

    private final MajorService majorService;

    /**
     * 查询专业列表
     * GET /api/majors
     */
    @GetMapping
    public Result<List<Major>> list(
            @RequestParam(required = false) Long departmentId
    ) {

        if (departmentId != null) {
            return Result.success(
                    majorService.lambdaQuery()
                            .eq(Major::getDepartmentId, departmentId)
                            .list()
            );
        }

        return Result.success(
                majorService.list()
        );
    }

    /**
     * 查询专业详情
     * GET /api/majors/{id}
     */
    @GetMapping("/{id}")
    public Result<Major> getById(
            @PathVariable Long id
    ) {
        return Result.success(
                majorService.getById(id)
        );
    }

    /**
     * 新增专业
     * POST /api/majors
     */
    @PostMapping
    public Result<Void> save(
            @RequestBody Major major
    ) {
        majorService.save(major);
        return Result.success();
    }

    /**
     * 修改专业
     * PUT /api/majors/{id}
     */
    @PutMapping("/{id}")
    public Result<Void> update(
            @PathVariable Long id,
            @RequestBody Major major
    ) {
        major.setId(id);
        majorService.updateById(major);
        return Result.success();
    }

    /**
     * 删除专业
     * DELETE /api/majors/{id}
     */
    @DeleteMapping("/{id}")
    public Result<Void> remove(
            @PathVariable Long id
    ) {
        majorService.removeById(id);
        return Result.success();
    }
}
