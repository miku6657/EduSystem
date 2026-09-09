package com.keshe.edumanage.service.base;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.keshe.edumanage.entity.base.Classroom;
import com.keshe.edumanage.mapper.base.ClassroomMapper;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 教室信息业务实现
 */
@Service
public class ClassroomServiceImpl extends ServiceImpl<ClassroomMapper, Classroom>
        implements ClassroomService {

    /** 教室使用状况：空闲 */
    public static final String STATUS_FREE = "空闲";

    @Override
    public Page<Classroom> pageClassrooms(Page<Classroom> page, String roomNo, Long campusId,
                                          String type, String status) {
        return lambdaQuery()
                .like(roomNo != null && !roomNo.isBlank(), Classroom::getRoomNo, roomNo)
                .eq(campusId != null, Classroom::getCampusId, campusId)
                .eq(type != null && !type.isBlank(), Classroom::getType, type)
                .eq(status != null && !status.isBlank(), Classroom::getStatus, status)
                .orderByDesc(Classroom::getId)
                .page(page);
    }

    @Override
    public List<Classroom> listFreeClassrooms(Long campusId, String type) {
        return lambdaQuery()
                .eq(campusId != null, Classroom::getCampusId, campusId)
                .eq(type != null && !type.isBlank(), Classroom::getType, type)
                .eq(Classroom::getStatus, STATUS_FREE)
                .list();
    }
}
