package com.keshe.edumanage.service.base;

import com.baomidou.mybatisplus.extension.service.IService;
import com.keshe.edumanage.entity.base.Term;

/**
 * 学期信息业务接口
 */
public interface TermService extends IService<Term> {

    /**
     * 查询当前学期（以系统日期落在学期起止日期内为准）
     *
     * @return 当前学期信息
     * @throws com.keshe.edumanage.common.exception.BusinessException 当前无进行中的学期时抛出
     */
    Term getCurrentTerm();
}
