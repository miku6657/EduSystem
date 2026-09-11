package com.keshe.edumanage.controller.exam;

import com.keshe.edumanage.common.result.Result;
import com.keshe.edumanage.entity.exam.ExamRoom;
import com.keshe.edumanage.service.exam.ExamRoomService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/exam-rooms")
@RequiredArgsConstructor
public class ExamRoomController {

    private final ExamRoomService examRoomService;

    /**
     * 查询考场列表
     * GET /api/exam-rooms
     */
    @GetMapping
    public Result<List<ExamRoom>> list(
            @RequestParam(required = false) Long examId
    ){
        if(examId != null){
            return Result.success(
                    examRoomService.listByExam(examId)
            );
        }
        return Result.success(
                examRoomService.list()
        );
    }

    /**
     * 查询考场详情
     * GET /api/exam-rooms/{id}
     */
    @GetMapping("/{id}")
    public Result<ExamRoom> getById(
            @PathVariable Long id
    ){
        return Result.success(
                examRoomService.getById(id)
        );
    }

    /**
     * 新增考场安排
     * POST /api/exam-rooms
     */
    @PostMapping
    public Result<Void> save(
            @RequestBody ExamRoom examRoom
    ){
        examRoomService.save(examRoom);
        return Result.success();
    }

    /**
     * 修改考场安排
     * PUT /api/exam-rooms/{id}
     */
    @PutMapping("/{id}")
    public Result<Void> update(
            @PathVariable Long id,
            @RequestBody ExamRoom examRoom
    ){
        examRoom.setId(id);
        examRoomService.updateById(examRoom);
        return Result.success();
    }

    /**
     * 删除考场安排
     * DELETE /api/exam-rooms/{id}
     */
    @DeleteMapping("/{id}")
    public Result<Void> remove(
            @PathVariable Long id
    ){
        examRoomService.removeById(id);
        return Result.success();
    }
}
