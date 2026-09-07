package com.keshe.edumanage.mapper;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.keshe.edumanage.entity.User;
import org.apache.ibatis.annotations.Mapper;


/**
 * 用户Mapper
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {


}