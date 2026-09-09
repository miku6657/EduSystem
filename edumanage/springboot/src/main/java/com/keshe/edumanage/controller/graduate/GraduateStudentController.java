package com.keshe.edumanage.controller.graduate;

import com.keshe.edumanage.common.result.Result;
import com.keshe.edumanage.entity.graduate.GraduateStudent;
import com.keshe.edumanage.service.graduate.GraduateStudentService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 毕业生管理接口
 * <p>毕业证编号生成、上报库查询</p>
 */
@RestController
@RequestMapping("/api/graduate-student")
@RequiredArgsConstructor
public class GraduateStudentController {

    private final GraduateStudentService graduateStudentService;

    /**
     * 新增毕业生记录
     */
    @PostMapping
    public Result<Void> save(@RequestBody GraduateStudent graduateStudent) {
        graduateStudentService.save(graduateStudent);
        return Result.success();
    }

    /**
     * 为单条记录生成毕业证编号
     */
    @PutMapping("/generate-no/{id}")
    public Result<String> generateNo(@PathVariable Long id) {
        return Result.success(graduateStudentService.generateCertificateNo(id));
    }

    /**
     * 批量为某届未编号的毕业生生成毕业证编号
     */
    @PutMapping("/batch-generate-no/{graduateYear}")
    public Result<Integer> batchGenerateNo(@PathVariable String graduateYear) {
        return Result.success(graduateStudentService.batchGenerateCertificateNo(graduateYear));
    }

    /**
     * 按届查询毕业生列表（生成上报库 / 打印毕业证）
     */
    @GetMapping("/list-by-year/{graduateYear}")
    public Result<List<GraduateStudent>> listByYear(@PathVariable String graduateYear) {
        return Result.success(graduateStudentService.listByYear(graduateYear));
    }

    /**
     * 修改毕业生记录
     */
    @PutMapping
    public Result<Void> update(@RequestBody GraduateStudent graduateStudent) {
        graduateStudentService.updateById(graduateStudent);
        return Result.success();
    }

    /**
     * 删除毕业生记录
     */
    @DeleteMapping("/{id}")
    public Result<Void> remove(@PathVariable Long id) {
        graduateStudentService.removeById(id);
        return Result.success();
    }
}
