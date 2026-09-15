package com.keshe.edumanage.controller.graduate;

import com.keshe.edumanage.common.result.Result;
import com.keshe.edumanage.entity.graduate.GraduateStudent;
import com.keshe.edumanage.service.graduate.GraduateStudentService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 毕业生管理接口
 *
 * 毕业证编号生成、毕业生上报库查询
 */
@RestController
@RequestMapping("/api/graduate-students")
@RequiredArgsConstructor
public class GraduateStudentController {

    private final GraduateStudentService graduateStudentService;


    /**
     * 新增毕业生记录
     *
     * POST /api/graduate-students
     */
    @PostMapping
    public Result<Void> save(
            @RequestBody GraduateStudent graduateStudent
    ) {
        graduateStudentService.save(graduateStudent);
        return Result.success();
    }


    /**
     * 生成单个毕业证编号
     *
     * PUT /api/graduate-students/{id}/certificate-no
     */
    @PutMapping("/{id}/certificate-no")
    public Result<String> generateNo(
            @PathVariable Long id
    ) {
        return Result.success(
                graduateStudentService.generateCertificateNo(id)
        );
    }


    /**
     * 批量生成毕业证编号
     *
     * PUT /api/graduate-students/year/{graduateYear}/certificate-no
     */
    @PutMapping("/year/{graduateYear}/certificate-no")
    public Result<Integer> batchGenerateNo(
            @PathVariable String graduateYear
    ) {
        return Result.success(
                graduateStudentService.batchGenerateCertificateNo(
                        graduateYear
                )
        );
    }


    /**
     * 按毕业年份查询毕业生列表
     *
     * GET /api/graduate-students/year/{graduateYear}
     */
    @GetMapping("/year/{graduateYear}")
    public Result<List<GraduateStudent>> listByYear(
            @PathVariable String graduateYear
    ) {
        return Result.success(
                graduateStudentService.listByYear(
                        graduateYear
                )
        );
    }


    /**
     * 修改毕业生记录
     *
     * PUT /api/graduate-students/{id}
     */
    @PutMapping("/{id}")
    public Result<Void> update(
            @PathVariable Long id,
            @RequestBody GraduateStudent graduateStudent
    ) {
        graduateStudent.setId(id);
        graduateStudentService.updateById(
                graduateStudent
        );
        return Result.success();
    }


    /**
     * 删除毕业生记录
     *
     * DELETE /api/graduate-students/{id}
     */
    @DeleteMapping("/{id}")
    public Result<Void> remove(
            @PathVariable Long id
    ) {
        graduateStudentService.removeById(id);
        return Result.success();
    }
}