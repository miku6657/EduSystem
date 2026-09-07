package com.keshe.edumanage.service.impl;

import com.keshe.edumanage.dao.entity.Department;
import com.keshe.edumanage.dao.mapper.DepartmentMapper;
import com.keshe.edumanage.service.DepartmentService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Service
public class DepartmentServiceImpl implements DepartmentService {
    @Resource
    private DepartmentMapper departmentMapper;

    @Override
    public Department findDptByid(String id) {
        return departmentMapper.findDptByid(id);
    }

    @Override
    public List<Department> findAllDpt() {
        return departmentMapper.findAllDpt();
    }
}
