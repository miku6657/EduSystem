package com.keshe.edumanage.service.base;


import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.keshe.edumanage.entity.base.ClassroomApply;
import com.keshe.edumanage.mapper.base.ClassroomApplyMapper;
import com.keshe.edumanage.service.base.ClassroomApplyService;
import org.springframework.stereotype.Service;


import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;


@Service
public class ClassroomApplyServiceImpl
        extends ServiceImpl<ClassroomApplyMapper, ClassroomApply>
        implements ClassroomApplyService {



    @Override
    public boolean checkConflict(
            Long roomId,
            LocalDate date,
            String timeSlot
    ){

        LambdaQueryWrapper<ClassroomApply> wrapper =
                new LambdaQueryWrapper<>();


        wrapper.eq(
                ClassroomApply::getRoomId,
                roomId
        );


        wrapper.eq(
                ClassroomApply::getApplyDate,
                date
        );


        wrapper.eq(
                ClassroomApply::getTimeSlot,
                timeSlot
        );


        wrapper.in(
                ClassroomApply::getStatus,
                Arrays.asList(
                        "待审核",
                        "已通过"
                )
        );


        return count(wrapper)>0;
    }



    @Override
    public List<ClassroomApply> listByApplicant(
            String username
    ){

        return lambdaQuery()
                .eq(
                        ClassroomApply::getApplicant,
                        username
                )
                .list();

    }

}