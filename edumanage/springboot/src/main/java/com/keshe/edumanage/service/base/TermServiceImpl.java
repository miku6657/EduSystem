package com.keshe.edumanage.service.base;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.keshe.edumanage.common.exception.BusinessException;
import com.keshe.edumanage.entity.base.Term;
import com.keshe.edumanage.mapper.base.TermMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

/**
 * 学期信息业务实现
 */
@Service
public class TermServiceImpl extends ServiceImpl<TermMapper, Term>
        implements TermService {

    @Override
    public Term getCurrentTerm() {
        LocalDate today = LocalDate.now();
        Term term = lambdaQuery()
                .le(Term::getStartDate, today)
                .ge(Term::getEndDate, today)
                .one();
        if (term == null) {
            throw new BusinessException("当前没有进行中的学期");
        }
        return term;
    }
}
