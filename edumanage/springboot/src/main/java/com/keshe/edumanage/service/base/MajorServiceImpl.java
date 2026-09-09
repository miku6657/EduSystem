package com.keshe.edumanage.service.base;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.keshe.edumanage.entity.base.Major;
import com.keshe.edumanage.mapper.base.MajorMapper;
import org.springframework.stereotype.Service;

/**
 * 专业信息业务实现
 */
@Service
public class MajorServiceImpl extends ServiceImpl<MajorMapper, Major>
        implements MajorService {
}
