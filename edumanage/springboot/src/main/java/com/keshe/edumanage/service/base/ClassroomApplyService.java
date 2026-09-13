package com.keshe.edumanage.service.base;


import com.baomidou.mybatisplus.extension.service.IService;
import com.keshe.edumanage.entity.base.ClassroomApply;

import java.time.LocalDate;
import java.util.List;


public interface ClassroomApplyService
        extends IService<ClassroomApply>{

    boolean checkConflict(
            Long roomId,
            LocalDate date,
            String timeSlot
    );

    List<ClassroomApply> listByApplicant(
            String username
    );

}