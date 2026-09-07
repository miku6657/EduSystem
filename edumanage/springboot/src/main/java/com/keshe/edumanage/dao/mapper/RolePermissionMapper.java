package com.keshe.edumanage.dao.mapper;

import com.keshe.edumanage.dao.entity.RolePermission;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface RolePermissionMapper {

    RolePermission findStuSelect();

    void addStuSelect();

    void deleteStuSelect();
}
