package com.keshe.edumanage.entity.attendance;
import com.baomidou.mybatisplus.annotation.TableName;
import com.keshe.edumanage.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.time.LocalDate;

@Data
@TableName("teaching_log")
@EqualsAndHashCode(callSuper = true)
public class TeachingLog extends BaseEntity {
    private Long teacherId;
    private Long courseId;
    private Long classId;
    private LocalDate teachingDate;
    private String content;
    private String homework;
}
