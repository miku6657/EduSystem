package com.keshe.edumanage.service.base;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.keshe.edumanage.common.exception.BusinessException;
import com.keshe.edumanage.entity.base.ClassInfo;
import com.keshe.edumanage.entity.base.Classroom;
import com.keshe.edumanage.entity.base.Course;
import com.keshe.edumanage.entity.base.CourseAdjust;
import com.keshe.edumanage.entity.base.Teacher;
import com.keshe.edumanage.mapper.base.CourseAdjustMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 调课申请业务实现
 */
@Service
@RequiredArgsConstructor
public class CourseAdjustServiceImpl extends ServiceImpl<CourseAdjustMapper, CourseAdjust>
        implements CourseAdjustService {

    private final CourseService courseService;
    private final ClassInfoService classInfoService;
    private final TeacherService teacherService;
    private final ClassroomService classroomService;

    @Override
    public void submit(CourseAdjust adjust) {
        if (adjust.getTeacherId() == null) {
            throw new BusinessException("缺少申请教师信息");
        }
        if (adjust.getCourseId() == null) {
            throw new BusinessException("请选择调课课程");
        }
        if (adjust.getOriginDate() == null || adjust.getTargetDate() == null) {
            throw new BusinessException("请填写原上课日期与调整后日期");
        }
        if (adjust.getOriginSlot() == null || adjust.getOriginSlot().isBlank()
                || adjust.getTargetSlot() == null || adjust.getTargetSlot().isBlank()) {
            throw new BusinessException("请填写原上课时段与调整后时段");
        }
        if (adjust.getOriginDate().equals(adjust.getTargetDate())
                && adjust.getOriginSlot().equals(adjust.getTargetSlot())) {
            throw new BusinessException("调整后的时间与原时间相同，无需调课");
        }
        adjust.setId(null);
        adjust.setStatus(STATUS_WAIT);
        adjust.setApproveRemark(null);
        save(adjust);
    }

    @Override
    public List<CourseAdjust> listByTeacher(Long teacherId) {
        List<CourseAdjust> list = lambdaQuery()
                .eq(CourseAdjust::getTeacherId, teacherId)
                .orderByDesc(CourseAdjust::getId)
                .list();
        enrich(list);
        return list;
    }

    @Override
    public List<CourseAdjust> listByStatus(String status) {
        List<CourseAdjust> list = lambdaQuery()
                .eq(status != null && !status.isBlank(), CourseAdjust::getStatus, status)
                .orderByDesc(CourseAdjust::getId)
                .list();
        enrich(list);
        return list;
    }

    @Override
    public void approve(Long id, String remark) {
        CourseAdjust adjust = requireWait(id);
        adjust.setStatus(STATUS_PASS);
        adjust.setApproveRemark(remark);
        updateById(adjust);
    }

    @Override
    public void reject(Long id, String remark) {
        CourseAdjust adjust = requireWait(id);
        adjust.setStatus(STATUS_FAIL);
        adjust.setApproveRemark(remark);
        updateById(adjust);
    }

    @Override
    public void cancel(Long id, Long teacherId) {
        CourseAdjust adjust = getById(id);
        if (adjust == null) {
            throw new BusinessException("调课申请不存在");
        }
        if (!Objects.equals(adjust.getTeacherId(), teacherId)) {
            throw new BusinessException("只能撤销自己的调课申请");
        }
        if (!STATUS_WAIT.equals(adjust.getStatus())) {
            throw new BusinessException("仅待审核的申请可以撤销");
        }
        adjust.setStatus(STATUS_CANCEL);
        updateById(adjust);
    }

    /** 取出待审核的申请，不存在或已审批则抛业务异常 */
    private CourseAdjust requireWait(Long id) {
        CourseAdjust adjust = getById(id);
        if (adjust == null) {
            throw new BusinessException("调课申请不存在");
        }
        if (!STATUS_WAIT.equals(adjust.getStatus())) {
            throw new BusinessException("该申请已审批，请勿重复操作");
        }
        return adjust;
    }

    /**
     * 填充展示字段：教师姓名、课程名称、班级名称、教室名称
     */
    private void enrich(List<CourseAdjust> list) {
        if (list == null || list.isEmpty()) {
            return;
        }
        Map<Long, Course> courseMap = nameMap(
                list.stream().map(CourseAdjust::getCourseId).filter(Objects::nonNull).distinct().toList(),
                ids -> courseService.listByIds(ids), Course::getId);
        Map<Long, ClassInfo> classMap = nameMap(
                list.stream().map(CourseAdjust::getClassId).filter(Objects::nonNull).distinct().toList(),
                ids -> classInfoService.listByIds(ids), ClassInfo::getId);
        Map<Long, Teacher> teacherMap = nameMap(
                list.stream().map(CourseAdjust::getTeacherId).filter(Objects::nonNull).distinct().toList(),
                ids -> teacherService.listByIds(ids), Teacher::getId);
        Map<Long, Classroom> roomMap = nameMap(
                list.stream().map(CourseAdjust::getClassroomId).filter(Objects::nonNull).distinct().toList(),
                ids -> classroomService.listByIds(ids), Classroom::getId);

        for (CourseAdjust adjust : list) {
            Course course = courseMap.get(adjust.getCourseId());
            if (course != null) {
                adjust.setCourseName(course.getName());
            }
            ClassInfo classInfo = classMap.get(adjust.getClassId());
            if (classInfo != null) {
                adjust.setClassName(classInfo.getName());
            }
            Teacher teacher = teacherMap.get(adjust.getTeacherId());
            if (teacher != null) {
                adjust.setTeacherName(teacher.getName());
            }
            Classroom room = roomMap.get(adjust.getClassroomId());
            if (room != null) {
                adjust.setRoomName(room.getRoomNo());
            }
        }
    }

    /** 通用：批量按 ID 取实体并转成 Map（避免逐行反查） */
    private <T> Map<Long, T> nameMap(List<Long> ids,
                                     java.util.function.Function<List<Long>, List<T>> loader,
                                     java.util.function.Function<T, Long> idGetter) {
        if (ids.isEmpty()) {
            return Map.of();
        }
        return loader.apply(ids).stream()
                .collect(Collectors.toMap(idGetter, Function.identity(), (a, b) -> a));
    }
}
