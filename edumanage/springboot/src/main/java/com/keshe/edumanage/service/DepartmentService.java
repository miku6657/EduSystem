package com.keshe.edumanage.service;

import com.keshe.edumanage.dao.entity.Department;

import java.util.List;

public interface DepartmentService {

    Department findDptByid(String id);

    List<Department> findAllDpt();

}
