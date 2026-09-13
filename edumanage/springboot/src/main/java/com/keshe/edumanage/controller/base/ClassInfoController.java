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
@RequestMapping("/api/classes")
@RequiredArgsConstructor
public class ClassInfoController {

    private final ClassInfoService classInfoService;

    /**
     * 分页条件查询班级
     * GET /api/classes
     */
    @GetMapping
    public Result<Page<ClassInfo>> page(
            @RequestParam(defaultValue = "1") Integer pageNo,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Long majorId,
            @RequestParam(required = false) Long campusId,
            @RequestParam(required = false) String grade
    ) {
        Page<ClassInfo> page = classInfoService.pageClasses(
                new Page<>(pageNo, pageSize),
                name,
                majorId,
                campusId,
                grade
        );
        return Result.success(page);
    }

    /**
     * 查询班级详情
     * GET /api/classes/{id}
     */
    @GetMapping("/{id}")
    public Result<ClassInfo> getById(
            @PathVariable Long id
    ) {
        return Result.success(
                classInfoService.getById(id)
        );
    }

    /**
     * 查询某专业下的班级
     * GET /api/classes?majorId=xxx
     */
    @GetMapping(params = "majorId")
    public Result<List<ClassInfo>> listByMajor(
            @RequestParam Long majorId
    ) {
        return Result.success(
                classInfoService.listByMajor(majorId)
        );
    }

    /**
     * 新增班级
     * POST /api/classes
     */
    @PostMapping
    public Result<Void> save(
            @RequestBody ClassInfo classInfo
    ) {
        classInfoService.save(classInfo);
        return Result.success();
    }

    /**
     * 修改班级
     * PUT /api/classes/{id}
     */
    @PutMapping("/{id}")
    public Result<Void> update(
            @PathVariable Long id,
            @RequestBody ClassInfo classInfo
    ) {
        classInfo.setId(id);
        classInfoService.updateById(classInfo);
        return Result.success();
    }

    /**
     * 删除班级
     * DELETE /api/classes/{id}
     */
    @DeleteMapping("/{id}")
    public Result<Void> remove(
            @PathVariable Long id
    ) {
        classInfoService.removeById(id);
        return Result.success();
    }
}
