package com.keshe.edumanage.service.base;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.keshe.edumanage.entity.base.ClassInfo;
import com.keshe.edumanage.mapper.base.ClassInfoMapper;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 班级信息业务实现
 */
@Service
public class ClassInfoServiceImpl extends ServiceImpl<ClassInfoMapper, ClassInfo>
        implements ClassInfoService {

    @Override
    public Page<ClassInfo> pageClasses(Page<ClassInfo> page, String name, Long majorId,
                                       Long campusId, String grade) {
        return lambdaQuery()
                .like(name != null && !name.isBlank(), ClassInfo::getName, name)
                .eq(majorId != null, ClassInfo::getMajorId, majorId)
                .eq(campusId != null, ClassInfo::getCampusId, campusId)
                .eq(grade != null && !grade.isBlank(), ClassInfo::getGrade, grade)
                .orderByDesc(ClassInfo::getId)
                .page(page);
    }

    @Override
    public List<ClassInfo> listByMajor(Long majorId) {
        return lambdaQuery()
                .eq(ClassInfo::getMajorId, majorId)
                .list();
    }
}
