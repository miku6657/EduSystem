package com.keshe.edumanage.service;

import com.keshe.edumanage.dao.entity.Term;

public interface TermService {
    Integer findLatestTerm();

    Term findActiveTerm();

    int insertTerm(Term term);

    void updateActiveTerm(String name);
}
