package com.keshe.edumanage.mapper.base;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.keshe.edumanage.entity.base.Course;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface CourseMapper extends BaseMapper<Course> {
}
