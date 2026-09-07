package com.keshe.edumanage.dao.mapper;

import com.keshe.edumanage.dao.entity.Term;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.dao.DataAccessException;

@Mapper
public interface TermMapper {

    Integer findLatestTerm();

    Term findActiveTerm();

    int insertTerm(Term term) throws DataAccessException;

    void updateActiveTerm(String name);
}
