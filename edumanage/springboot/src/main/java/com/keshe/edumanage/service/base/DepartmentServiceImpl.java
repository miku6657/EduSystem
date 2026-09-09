package com.keshe.edumanage.service.base;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.keshe.edumanage.entity.base.Department;
import com.keshe.edumanage.mapper.base.DepartmentMapper;
import org.springframework.stereotype.Service;

/**
 * 系部信息业务实现
 */
@Service
public class DepartmentServiceImpl extends ServiceImpl<DepartmentMapper, Department>
        implements DepartmentService {
}
