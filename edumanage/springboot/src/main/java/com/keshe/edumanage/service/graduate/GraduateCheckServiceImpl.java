package com.keshe.edumanage.service.graduate;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.keshe.edumanage.common.exception.BusinessException;
import com.keshe.edumanage.entity.graduate.GraduateCheck;
import com.keshe.edumanage.mapper.graduate.GraduateCheckMapper;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 毕业资格审核业务实现
 */
@Service
public class GraduateCheckServiceImpl extends ServiceImpl<GraduateCheckMapper, GraduateCheck>
        implements GraduateCheckService {

    @Override
    public void audit(GraduateCheck check) {
        if (check.getStudentId() == null) {
            throw new BusinessException("学生信息不能为空");
        }
        if (!STATUS_PASS.equals(check.getCheckStatus())
                && !STATUS_FAIL.equals(check.getCheckStatus())
                && !STATUS_WAIT.equals(check.getCheckStatus())) {
            throw new BusinessException("审核结论只能是 WAIT / PASS / FAIL");
        }
        // 学分或课程审核未通过时，毕业资格结论不允许为通过
        if (STATUS_PASS.equals(check.getCheckStatus())
                && (!STATUS_PASS.equals(check.getCreditStatus())
                    || !STATUS_PASS.equals(check.getCourseStatus()))) {
            throw new BusinessException("学分或课程审核未通过，毕业资格审核不能通过");
        }
        // 同一学生只保留一条审核记录
        GraduateCheck exist = getByStudent(check.getStudentId());
        if (exist != null) {
            check.setId(exist.getId());
        }
        saveOrUpdate(check);
    }

    @Override
    public List<GraduateCheck> listByStatus(String checkStatus) {
        return lambdaQuery()
                .eq(checkStatus != null && !checkStatus.isBlank(),
                        GraduateCheck::getCheckStatus, checkStatus)
                .orderByDesc(GraduateCheck::getId)
                .list();
    }

    @Override
    public GraduateCheck getByStudent(Long studentId) {
        return lambdaQuery()
                .eq(GraduateCheck::getStudentId, studentId)
                .one();
    }
}
