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
@RequestMapping("/api/classrooms")
@RequiredArgsConstructor
public class ClassroomController {

    private final ClassroomService classroomService;

    /**
     * 分页查询教室
     * GET /api/classrooms
     */
    @GetMapping
    public Result<Page<Classroom>> page(
            @RequestParam(defaultValue = "1") Integer pageNo,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String roomNo,
            @RequestParam(required = false) Long campusId,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String status
    ) {
        Page<Classroom> page =
                classroomService.pageClassrooms(
                        new Page<>(pageNo, pageSize),
                        roomNo,
                        campusId,
                        type,
                        status
                );
        return Result.success(page);
    }

    /**
     * 查询教室详情
     * GET /api/classrooms/{id}
     */
    @GetMapping("/{id}")
    public Result<Classroom> getById(
            @PathVariable Long id
    ) {
        return Result.success(
                classroomService.getById(id)
        );
    }

    /**
     * 查询空闲教室
     * GET /api/classrooms/free
     */
    @GetMapping("/free")
    public Result<List<Classroom>> listFree(
            @RequestParam(required = false) Long campusId,
            @RequestParam(required = false) String type
    ) {
        return Result.success(
                classroomService.listFreeClassrooms(
                        campusId,
                        type
                )
        );
    }

    /**
     * 新增教室
     * POST /api/classrooms
     */
    @PostMapping
    public Result<Void> save(
            @RequestBody Classroom classroom
    ) {
        classroomService.save(classroom);
        return Result.success();
    }

    /**
     * 修改教室
     * PUT /api/classrooms/{id}
     */
    @PutMapping("/{id}")
    public Result<Void> update(
            @PathVariable Long id,
            @RequestBody Classroom classroom
    ) {
        classroom.setId(id);
        classroomService.updateById(classroom);
        return Result.success();
    }

    /**
     * 删除教室
     * DELETE /api/classrooms/{id}
     */
    @DeleteMapping("/{id}")
    public Result<Void> remove(
            @PathVariable Long id
    ) {
        classroomService.removeById(id);
        return Result.success();
    }
}
