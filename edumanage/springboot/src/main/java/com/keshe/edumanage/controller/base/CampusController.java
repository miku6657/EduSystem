package com.keshe.edumanage.controller.base;

import com.keshe.edumanage.common.result.Result;
import com.keshe.edumanage.entity.base.Campus;
import com.keshe.edumanage.service.base.CampusService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/campuses")
@RequiredArgsConstructor
public class CampusController {

    private final CampusService campusService;

    /**
     * 查询校区列表
     * GET /api/campuses
     */
    @GetMapping
    public Result<List<Campus>> list() {
        return Result.success(campusService.list());
    }

    /**
     * 查询校区详情
     * GET /api/campuses/{id}
     */
    @GetMapping("/{id}")
    public Result<Campus> getById(
            @PathVariable Long id
    ) {
        return Result.success(
                campusService.getById(id)
        );
    }

    /**
     * 新增校区
     * POST /api/campuses
     */
    @PostMapping
    public Result<Void> save(
            @RequestBody Campus campus
    ) {
        campusService.save(campus);
        return Result.success();
    }

    /**
     * 修改校区
     * PUT /api/campuses/{id}
     */
    @PutMapping("/{id}")
    public Result<Void> update(
            @PathVariable Long id,
            @RequestBody Campus campus
    ) {
        campus.setId(id);
        campusService.updateById(campus);
        return Result.success();
    }

    /**
     * 删除校区
     * DELETE /api/campuses/{id}
     */
    @DeleteMapping("/{id}")
    public Result<Void> remove(
            @PathVariable Long id
    ) {
        campusService.removeById(id);
        return Result.success();
    }
}
