package com.keshe.edumanage.controller.base;

import com.keshe.edumanage.common.result.Result;
import com.keshe.edumanage.entity.base.Term;
import com.keshe.edumanage.service.base.TermService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 学期信息接口
 */
@RestController
@RequestMapping("/api/terms")
@RequiredArgsConstructor
public class TermController {

    private final TermService termService;

    /**
     * 查询学期列表
     * GET /api/terms
     */
    @GetMapping
    public Result<List<Term>> list() {
        return Result.success(termService.list());
    }

    /**
     * 查询学期详情
     * GET /api/terms/{id}
     */
    @GetMapping("/{id}")
    public Result<Term> getById(
            @PathVariable Long id
    ) {
        return Result.success(termService.getById(id));
    }

    /**
     * 新增学期
     * POST /api/terms
     */
    @PostMapping
    public Result<Void> save(
            @RequestBody Term term
    ) {
        termService.save(term);
        return Result.success();
    }

    /**
     * 修改学期
     * PUT /api/terms/{id}
     */
    @PutMapping("/{id}")
    public Result<Void> update(
            @PathVariable Long id,
            @RequestBody Term term
    ) {
        term.setId(id);
        termService.updateById(term);
        return Result.success();
    }

    /**
     * 删除学期
     * DELETE /api/terms/{id}
     */
    @DeleteMapping("/{id}")
    public Result<Void> remove(
            @PathVariable Long id
    ) {
        termService.removeById(id);
        return Result.success();
    }
}
