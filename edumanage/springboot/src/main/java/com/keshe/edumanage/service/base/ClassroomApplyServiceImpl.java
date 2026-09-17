package com.keshe.edumanage.service.base;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.keshe.edumanage.entity.base.Classroom;
import com.keshe.edumanage.entity.base.ClassroomApply;
import com.keshe.edumanage.entity.base.Student;
import com.keshe.edumanage.entity.base.Teacher;
import com.keshe.edumanage.entity.system.User;
import com.keshe.edumanage.mapper.base.ClassroomApplyMapper;
import com.keshe.edumanage.service.UserService;
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

    private final UserService userService;

    private final StudentService studentService;

    private final TeacherService teacherService;

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
                .eq(
                        ClassroomApply::getApplicant,
                        username
                )
                .orderByDesc(
                        ClassroomApply::getCreateTime
                )
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
        vo.setApplicantName(
                applicantNameOf(
                        apply.getApplicant()
                )
        );
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

    /**
     * 根据登录账号解析真实姓名。
     *
     * 新身份体系：
     *
     * applicant
     * ↓
     * sys_user.username
     * ↓
     * sys_user.business_id
     * ↓
     * STUDENT -> base_student
     * TEACHER -> base_teacher
     */
    private String applicantNameOf(
            String username
    ) {

        if (
                username == null
                || username.isBlank()
        ) {
            return null;
        }

        User user =
                userService.findByUsername(
                        username
                );

        /**
         * 找不到sys_user时，
         * 至少返回原账号，
         * 页面不会空白。
         */
        if (user == null) {
            return username;
        }

        Long businessId =
                user.getBusinessId();

        if (businessId == null) {
            return username;
        }

        String role =
                user.getRole();

        /**
         * 学生
         */
        if (
                "STUDENT".equalsIgnoreCase(
                        role
                )
        ) {

            Student student =
                    studentService.getById(
                            businessId
                    );

            if (
                    student != null
                    && student.getName() != null
            ) {
                return student.getName();
            }
        }

        /**
         * 教师
         */
        if (
                "TEACHER".equalsIgnoreCase(
                        role
                )
        ) {

            Teacher teacher =
                    teacherService.getById(
                            businessId
                    );

            if (
                    teacher != null
                    && teacher.getName() != null
            ) {
                return teacher.getName();
            }
        }

        return username;
    }
}
