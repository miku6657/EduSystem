package com.keshe.edumanage.entity.attendance;
import com.baomidou.mybatisplus.annotation.TableName;
import com.keshe.edumanage.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("teacher_attendance")
@EqualsAndHashCode(callSuper = true)
public class TeacherAttendance extends BaseEntity {
    private Long teacherId;
    private LocalDate attendanceDate;
    private String status;
    private LocalDateTime checkTime;
}
