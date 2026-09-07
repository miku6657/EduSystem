package com.keshe.edumanage.service.impl;

import com.keshe.edumanage.dao.entity.RolePermission;
import com.keshe.edumanage.dao.mapper.RolePermissionMapper;
import com.keshe.edumanage.service.RolePermissionService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
public class RolePermissionServiceImpl implements RolePermissionService {

    @Resource
    RolePermissionMapper rolePermissionMapper;

    @Override
    public RolePermission findStuSelect() {
        return rolePermissionMapper.findStuSelect();
    }

    @Override
    public void addStuSelect() {
        rolePermissionMapper.addStuSelect();
    }

    @Override
    public void deleteStuSelect() {
        rolePermissionMapper.deleteStuSelect();
    }
}
