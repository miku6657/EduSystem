package com.keshe.edumanage.service.base;


import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.keshe.edumanage.common.exception.BusinessException;
import com.keshe.edumanage.entity.base.ClassroomApply;
import com.keshe.edumanage.mapper.base.ClassroomApplyMapper;
import com.keshe.edumanage.service.base.ClassroomApplyService;
import org.springframework.stereotype.Service;


import java.time.LocalDate;
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

        if (roomId == null || date == null || timeSlot == null) {
            return false;
        }

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
                List.of(
                        STATUS_WAIT,
                        STATUS_PASS
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
                .orderByDesc(
                        ClassroomApply::getId
                )
                .list();

    }



    @Override
    public List<ClassroomApply> listByStatus(
            String status
    ){

        return lambdaQuery()
                .eq(
                        status != null && !status.isBlank(),
                        ClassroomApply::getStatus,
                        status
                )
                .orderByDesc(
                        ClassroomApply::getId
                )
                .list();

    }



    @Override
    public void cancel(
            Long id,
            String username
    ){

        ClassroomApply apply =
                getById(id);


        if (apply == null) {
            throw new BusinessException(
                    "申请记录不存在"
            );
        }


        if (!apply.getApplicant().equals(username)) {
            throw new BusinessException(
                    "只能撤回自己的申请"
            );
        }


        if (!STATUS_WAIT.equals(apply.getStatus())) {
            throw new BusinessException(
                    "仅待审核的申请可以撤回"
            );
        }


        apply.setStatus(
                STATUS_CANCEL
        );


        updateById(apply);

    }

}
