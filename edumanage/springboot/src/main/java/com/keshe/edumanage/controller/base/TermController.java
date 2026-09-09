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
@RequestMapping("/api/term")
@RequiredArgsConstructor
public class TermController {

    private final TermService termService;

    /**
     * 查询全部学期
     */
    @GetMapping("/list")
    public Result<List<Term>> list() {
        return Result.success(termService.list());
    }

    /**
     * 查询当前学期
     */
    @GetMapping("/current")
    public Result<Term> current() {
        return Result.success(termService.getCurrentTerm());
    }

    /**
     * 新增学期
     */
    @PostMapping
    public Result<Void> save(@RequestBody Term term) {
        termService.save(term);
        return Result.success();
    }

    /**
     * 修改学期
     */
    @PutMapping
    public Result<Void> update(@RequestBody Term term) {
        termService.updateById(term);
        return Result.success();
    }

    /**
     * 删除学期
     */
    @DeleteMapping("/{id}")
    public Result<Void> remove(@PathVariable Long id) {
        termService.removeById(id);
        return Result.success();
    }
}
