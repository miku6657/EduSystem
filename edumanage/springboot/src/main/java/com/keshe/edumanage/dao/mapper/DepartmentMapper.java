package com.keshe.edumanage.dao.mapper;

import com.keshe.edumanage.dao.entity.Department;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface DepartmentMapper {

    Department findDptByid(@Param("id") String id);

    List<Department> findAllDpt();
}
