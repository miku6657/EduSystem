package com.keshe.edumanage.service.base;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.keshe.edumanage.entity.base.Campus;
import com.keshe.edumanage.mapper.base.CampusMapper;
import org.springframework.stereotype.Service;

/**
 * 校区信息业务实现
 */
@Service
public class CampusServiceImpl extends ServiceImpl<CampusMapper, Campus>
        implements CampusService {
}
