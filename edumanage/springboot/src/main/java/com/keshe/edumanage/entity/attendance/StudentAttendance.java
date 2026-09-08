package com.keshe.edumanage.entity.attendance;


import com.baomidou.mybatisplus.annotation.TableName;
import com.keshe.edumanage.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;


@Data
@TableName("student_attendance")
@EqualsAndHashCode(callSuper = true)
public class StudentAttendance extends BaseEntity {


    private Long studentId;


    private Long courseId;


    private LocalDate attendanceDate;


    private String status;

}