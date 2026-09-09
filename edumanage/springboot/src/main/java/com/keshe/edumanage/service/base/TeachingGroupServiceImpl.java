package com.keshe.edumanage.service.base;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.keshe.edumanage.entity.base.TeachingGroup;
import com.keshe.edumanage.mapper.base.TeachingGroupMapper;
import org.springframework.stereotype.Service;

/**
 * 教研室信息业务实现
 */
@Service
public class TeachingGroupServiceImpl extends ServiceImpl<TeachingGroupMapper, TeachingGroup>
        implements TeachingGroupService {
}
