package com.keshe.edumanage.service.impl;

import com.keshe.edumanage.dao.entity.Permission;
import com.keshe.edumanage.dao.mapper.PermissionMapper;
import com.keshe.edumanage.service.PermissionService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Service
public class PermissionServiceImpl implements PermissionService {
    @Resource
    private PermissionMapper permissionMapper;

    @Override
    public List<Permission> findPermsByRid(String rid) {
        return permissionMapper.findPermsByRid(rid);
    }
}
