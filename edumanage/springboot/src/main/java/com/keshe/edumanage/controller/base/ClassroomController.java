package com.keshe.edumanage.controller.base;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.keshe.edumanage.common.result.Result;
import com.keshe.edumanage.entity.base.Classroom;
import com.keshe.edumanage.service.base.ClassroomService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 教室信息接口
 */
@RestController
@RequestMapping("/api/classroom")
@RequiredArgsConstructor
public class ClassroomController {

    private final ClassroomService classroomService;

    /**
     * 分页条件查询教室（支持按编号、校区、类型、使用状况过滤）
     */
    @GetMapping("/page")
    public Result<Page<Classroom>> page(
            @RequestParam(defaultValue = "1") Integer pageNo,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String roomNo,
            @RequestParam(required = false) Long campusId,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String status) {
        Page<Classroom> page = classroomService.pageClassrooms(
                new Page<>(pageNo, pageSize), roomNo, campusId, type, status);
        return Result.success(page);
    }

    /**
     * 根据ID查询教室
     */
    @GetMapping("/{id}")
    public Result<Classroom> getById(@PathVariable Long id) {
        return Result.success(classroomService.getById(id));
    }

    /**
     * 查询某校区下指定类型的空闲教室
     */
    @GetMapping("/free")
    public Result<List<Classroom>> listFree(
            @RequestParam(required = false) Long campusId,
            @RequestParam(required = false) String type) {
        return Result.success(classroomService.listFreeClassrooms(campusId, type));
    }

    /**
     * 新增教室
     */
    @PostMapping
    public Result<Void> save(@RequestBody Classroom classroom) {
        classroomService.save(classroom);
        return Result.success();
    }

    /**
     * 修改教室
     */
    @PutMapping
    public Result<Void> update(@RequestBody Classroom classroom) {
        classroomService.updateById(classroom);
        return Result.success();
    }

    /**
     * 删除教室
     */
    @DeleteMapping("/{id}")
    public Result<Void> remove(@PathVariable Long id) {
        classroomService.removeById(id);
        return Result.success();
    }
}
