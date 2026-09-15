package com.keshe.edumanage.service.base;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.keshe.edumanage.entity.base.Classroom;
import com.keshe.edumanage.entity.base.ClassroomApply;
import com.keshe.edumanage.mapper.base.ClassroomApplyMapper;
import com.keshe.edumanage.vo.ClassroomApplyVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

/**
 * 教室申请业务实现
 */
@Service
@RequiredArgsConstructor
public class ClassroomApplyServiceImpl
        extends ServiceImpl<ClassroomApplyMapper, ClassroomApply>
        implements ClassroomApplyService {

    private final ClassroomService classroomService;

    @Override
    public boolean checkConflict(
            Long roomId,
            LocalDate date,
            String timeSlot
    ) {
        LambdaQueryWrapper<ClassroomApply> wrapper =
                new LambdaQueryWrapper<>();

        wrapper.eq(ClassroomApply::getRoomId, roomId);
        wrapper.eq(ClassroomApply::getApplyDate, date);
        wrapper.eq(ClassroomApply::getTimeSlot, timeSlot);
        wrapper.in(ClassroomApply::getStatus, Arrays.asList("待审核", "已通过"));

        return count(wrapper) > 0;
    }

    @Override
    public List<ClassroomApply> listByApplicant(
            String username
    ) {
        return lambdaQuery()
                .eq(ClassroomApply::getApplicant, username)
                .list();
    }

    @Override
    public List<ClassroomApply> listByStatus(
            String status
    ) {
        return lambdaQuery()
                .eq(status != null && !status.isBlank(),
                        ClassroomApply::getStatus, status)
                .orderByDesc(ClassroomApply::getCreateTime)
                .list();
    }

    @Override
    public List<ClassroomApplyVO> listVO(
            String status
    ) {
        return listByStatus(status)
                .stream()
                .map(this::toVO)
                .toList();
    }

    @Override
    public List<ClassroomApplyVO> listByApplicantVO(
            String username
    ) {
        return listByApplicant(username)
                .stream()
                .map(this::toVO)
                .toList();
    }

    /**
     * 实体转 VO，并补充教室名称
     */
    private ClassroomApplyVO toVO(ClassroomApply apply) {
        ClassroomApplyVO vo = new ClassroomApplyVO();
        vo.setId(apply.getId());
        vo.setRoomId(apply.getRoomId());
        vo.setRoomName(roomNameOf(apply.getRoomId()));
        vo.setApplicant(apply.getApplicant());
        vo.setClassName(apply.getClassName());
        vo.setDate(apply.getApplyDate());
        vo.setTimeSlot(apply.getTimeSlot());
        vo.setPurpose(apply.getPurpose());
        vo.setReason(apply.getReason());
        vo.setStatus(apply.getStatus());
        vo.setApplyTime(apply.getCreateTime());
        return vo;
    }

    /**
     * 根据教室ID查询教室编号（教室无 name 字段，房间名即 roomNo）
     */
    private String roomNameOf(Long roomId) {
        if (roomId == null) {
            return null;
        }
        Classroom classroom = classroomService.getById(roomId);
        return classroom == null ? null : classroom.getRoomNo();
    }
}
