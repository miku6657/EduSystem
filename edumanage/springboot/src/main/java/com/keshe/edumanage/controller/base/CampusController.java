package com.keshe.edumanage.controller.base;

import com.keshe.edumanage.common.result.Result;
import com.keshe.edumanage.entity.base.Campus;
import com.keshe.edumanage.service.base.CampusService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 校区信息接口
 */
@RestController
@RequestMapping("/api/campus")
@RequiredArgsConstructor
public class CampusController {

    private final CampusService campusService;

    /**
     * 查询全部校区
     */
    @GetMapping("/list")
    public Result<List<Campus>> list() {
        return Result.success(campusService.list());
    }

    /**
     * 根据ID查询校区
     */
    @GetMapping("/{id}")
    public Result<Campus> getById(@PathVariable Long id) {
        return Result.success(campusService.getById(id));
    }

    /**
     * 新增校区
     */
    @PostMapping
    public Result<Void> save(@RequestBody Campus campus) {
        campusService.save(campus);
        return Result.success();
    }

    /**
     * 修改校区
     */
    @PutMapping
    public Result<Void> update(@RequestBody Campus campus) {
        campusService.updateById(campus);
        return Result.success();
    }

    /**
     * 删除校区
     */
    @DeleteMapping("/{id}")
    public Result<Void> remove(@PathVariable Long id) {
        campusService.removeById(id);
        return Result.success();
    }
}
