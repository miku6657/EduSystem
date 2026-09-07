package com.keshe.edumanage.service;

import com.keshe.edumanage.dao.entity.Permission;

import java.util.List;

public interface PermissionService {

    List<Permission> findPermsByRid(String rid);
}
