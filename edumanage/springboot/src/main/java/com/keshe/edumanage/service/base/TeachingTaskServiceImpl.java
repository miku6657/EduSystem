package com.keshe.edumanage.service.base;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.keshe.edumanage.entity.base.ClassInfo;
import com.keshe.edumanage.entity.base.Classroom;
import com.keshe.edumanage.entity.base.Course;
import com.keshe.edumanage.entity.base.Teacher;
import com.keshe.edumanage.entity.base.TeachingTask;
import com.keshe.edumanage.mapper.base.TeachingTaskMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 教学任务（任课关系 + 上课时间）业务实现
 */
@Service
@RequiredArgsConstructor
public class TeachingTaskServiceImpl extends ServiceImpl<TeachingTaskMapper, TeachingTask>
        implements TeachingTaskService {

    private final ClassInfoService classInfoService;
    private final CourseService courseService;
    private final TeacherService teacherService;
    private final ClassroomService classroomService;

    @Override
    public List<TeachingTask> listByTeacher(Long teacherId, Long termId) {
        List<TeachingTask> tasks = lambdaQuery()
                .eq(TeachingTask::getTeacherId, teacherId)
                .eq(termId != null, TeachingTask::getTermId, termId)
                .orderByAsc(TeachingTask::getWeekday)
                .orderByAsc(TeachingTask::getStartSection)
                .orderByAsc(TeachingTask::getId)
                .list();
        enrich(tasks);
        return tasks;
    }

    @Override
    public List<TeachingTask> listByClass(Long classId, Long termId) {
        List<TeachingTask> tasks = lambdaQuery()
                .eq(TeachingTask::getClassId, classId)
                .eq(termId != null, TeachingTask::getTermId, termId)
                .orderByAsc(TeachingTask::getWeekday)
                .orderByAsc(TeachingTask::getStartSection)
                .orderByAsc(TeachingTask::getId)
                .list();
        enrich(tasks);
        return tasks;
    }

    @Override
    public List<ClassInfo> listMyClasses(Long teacherId, Long termId) {
        List<Long> classIds = lambdaQuery()
                .eq(TeachingTask::getTeacherId, teacherId)
                .eq(termId != null, TeachingTask::getTermId, termId)
                .list()
                .stream()
                .map(TeachingTask::getClassId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        if (classIds.isEmpty()) {
            return List.of();
        }
        return classInfoService.listByIds(classIds);
    }

    @Override
    public List<Course> listMyCourses(Long teacherId, Long termId) {
        List<Long> courseIds = lambdaQuery()
                .eq(TeachingTask::getTeacherId, teacherId)
                .eq(termId != null, TeachingTask::getTermId, termId)
                .list()
                .stream()
                .map(TeachingTask::getCourseId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        if (courseIds.isEmpty()) {
            return List.of();
        }
        return courseService.listByIds(courseIds);
    }

    @Override
    public boolean teachesCourse(Long teacherId, Long courseId) {
        if (teacherId == null || courseId == null) {
            return false;
        }
        return lambdaQuery()
                .eq(TeachingTask::getTeacherId, teacherId)
                .eq(TeachingTask::getCourseId, courseId)
                .exists();
    }

    /**
     * 填充展示字段：课程名、班级名、教师名、教室名（课表直接可用）
     */
    private void enrich(List<TeachingTask> tasks) {
        if (tasks == null || tasks.isEmpty()) {
            return;
        }
        Map<Long, Course> courseMap = loadMap(
                tasks.stream().map(TeachingTask::getCourseId).filter(Objects::nonNull).distinct().toList(),
                ids -> courseService.listByIds(ids), Course::getId);
        Map<Long, ClassInfo> classMap = loadMap(
                tasks.stream().map(TeachingTask::getClassId).filter(Objects::nonNull).distinct().toList(),
                ids -> classInfoService.listByIds(ids), ClassInfo::getId);
        Map<Long, Teacher> teacherMap = loadMap(
                tasks.stream().map(TeachingTask::getTeacherId).filter(Objects::nonNull).distinct().toList(),
                ids -> teacherService.listByIds(ids), Teacher::getId);
        Map<Long, Classroom> roomMap = loadMap(
                tasks.stream().map(TeachingTask::getClassroomId).filter(Objects::nonNull).distinct().toList(),
                ids -> classroomService.listByIds(ids), Classroom::getId);

        for (TeachingTask task : tasks) {
            Course course = courseMap.get(task.getCourseId());
            if (course != null) {
                task.setCourseName(course.getName());
            }
            ClassInfo classInfo = classMap.get(task.getClassId());
            if (classInfo != null) {
                task.setClassName(classInfo.getName());
            }
            Teacher teacher = teacherMap.get(task.getTeacherId());
            if (teacher != null) {
                task.setTeacherName(teacher.getName());
            }
            Classroom room = roomMap.get(task.getClassroomId());
            if (room != null) {
                task.setRoomName(room.getRoomNo());
            }
        }
    }

    /** 通用：批量按 ID 取实体并转成 Map（避免逐行反查） */
    private <T> Map<Long, T> loadMap(List<Long> ids,
                                     Function<List<Long>, List<T>> loader,
                                     Function<T, Long> idGetter) {
        if (ids.isEmpty()) {
            return Map.of();
        }
        return loader.apply(ids).stream()
                .collect(Collectors.toMap(idGetter, Function.identity(), (a, b) -> a));
    }
}
