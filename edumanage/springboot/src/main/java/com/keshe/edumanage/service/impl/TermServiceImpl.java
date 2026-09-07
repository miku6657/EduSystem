package com.keshe.edumanage.service.impl;

import com.keshe.edumanage.dao.entity.Term;
import com.keshe.edumanage.dao.mapper.TermMapper;
import com.keshe.edumanage.service.TermService;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
public class TermServiceImpl implements TermService {

    @Resource
    private TermMapper termMapper;

    @Override
    public Integer findLatestTerm() {
        return termMapper.findLatestTerm();
    }

    @Override
    public Term findActiveTerm() {
        return termMapper.findActiveTerm();
    }

    @Override
    public int insertTerm(Term term) throws DataAccessException {
        return termMapper.insertTerm(term);
    }

    @Override
    public void updateActiveTerm(String name) {
        termMapper.updateActiveTerm(name);
    }
}
