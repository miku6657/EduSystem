package com.keshe.edumanage.service.graduate;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.keshe.edumanage.common.exception.BusinessException;
import com.keshe.edumanage.entity.graduate.GraduateStudent;
import com.keshe.edumanage.mapper.graduate.GraduateStudentMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 毕业生信息业务实现
 */
@Service
public class GraduateStudentServiceImpl extends ServiceImpl<GraduateStudentMapper, GraduateStudent>
        implements GraduateStudentService {

    /** 毕业证编号流水号位数 */
    private static final int CERTIFICATE_NO_LENGTH = 6;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String generateCertificateNo(Long id) {
        GraduateStudent graduateStudent = getById(id);
        if (graduateStudent == null) {
            throw new BusinessException("毕业生记录不存在");
        }
        if (graduateStudent.getGraduateYear() == null || graduateStudent.getGraduateYear().isBlank()) {
            throw new BusinessException("毕业年份为空，无法生成毕业证编号");
        }
        String certificateNo = graduateStudent.getGraduateYear()
                + String.format("%0" + CERTIFICATE_NO_LENGTH + "d", graduateStudent.getId());
        // 编号唯一性校验
        boolean exists = lambdaQuery()
                .eq(GraduateStudent::getCertificateNo, certificateNo)
                .ne(GraduateStudent::getId, graduateStudent.getId())
                .exists();
        if (exists) {
            throw new BusinessException("毕业证编号已被占用：" + certificateNo);
        }
        graduateStudent.setCertificateNo(certificateNo);
        updateById(graduateStudent);
        return certificateNo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int batchGenerateCertificateNo(String graduateYear) {
        List<GraduateStudent> list = lambdaQuery()
                .eq(GraduateStudent::getGraduateYear, graduateYear)
                .isNull(GraduateStudent::getCertificateNo)
                .list();
        for (GraduateStudent graduateStudent : list) {
            String certificateNo = graduateStudent.getGraduateYear()
                    + String.format("%0" + CERTIFICATE_NO_LENGTH + "d", graduateStudent.getId());
            graduateStudent.setCertificateNo(certificateNo);
        }
        if (!list.isEmpty()) {
            updateBatchById(list);
        }
        return list.size();
    }

    @Override
    public List<GraduateStudent> listByYear(String graduateYear) {
        return lambdaQuery()
                .eq(GraduateStudent::getGraduateYear, graduateYear)
                .orderByAsc(GraduateStudent::getId)
                .list();
    }
}
