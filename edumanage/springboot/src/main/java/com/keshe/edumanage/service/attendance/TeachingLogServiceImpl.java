package com.keshe.edumanage.service.attendance;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.keshe.edumanage.common.exception.BusinessException;
import com.keshe.edumanage.entity.attendance.TeachingLog;
import com.keshe.edumanage.mapper.attendance.TeachingLogMapper;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;

/**
 * 教学日志业务实现
 */
@Service
public class TeachingLogServiceImpl extends ServiceImpl<TeachingLogMapper, TeachingLog>
        implements TeachingLogService {

    @Override
    public void addLog(TeachingLog teachingLog) {
        if (teachingLog.getTeachingDate() == null) {
            throw new BusinessException("授课日期不能为空");
        }
        // 授课日志须课后填写（最迟每周末提交），不允许补填未来日期
        if (teachingLog.getTeachingDate().isAfter(LocalDate.now())) {
            throw new BusinessException("授课日期不能晚于今天，日志须课后填写");
        }
        teachingLog.setId(null);
        save(teachingLog);
    }

    @Override
    public List<TeachingLog> listByTeacherWeek(Long teacherId, LocalDate anyDayOfWeek) {
        LocalDate[] weekRange = getWeekRange(anyDayOfWeek);
        return lambdaQuery()
                .eq(TeachingLog::getTeacherId, teacherId)
                .between(TeachingLog::getTeachingDate, weekRange[0], weekRange[1])
                .orderByAsc(TeachingLog::getTeachingDate)
                .list();
    }

    @Override
    public List<TeachingLog> listByClassWeek(Long classId, LocalDate anyDayOfWeek) {
        LocalDate[] weekRange = getWeekRange(anyDayOfWeek);
        return lambdaQuery()
                .eq(TeachingLog::getClassId, classId)
                .between(TeachingLog::getTeachingDate, weekRange[0], weekRange[1])
                .orderByAsc(TeachingLog::getTeachingDate)
                .list();
    }

    /**
     * 计算某天所在周的周一至周日区间
     */
    private LocalDate[] getWeekRange(LocalDate anyDayOfWeek) {
        LocalDate monday = anyDayOfWeek.with(DayOfWeek.MONDAY);
        return new LocalDate[]{monday, monday.plusDays(6)};
    }
}
