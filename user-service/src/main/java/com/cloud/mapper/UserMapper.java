package com.cloud.mapper;

import com.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface UserMapper {

    @Select("select uid,name,age,sex from db_user where uid = #{uid}")
    User getUserById(Integer uid);

    @Select("select can_borrow from db_user where uid = #{uid}")
    Integer getUserBookRemain(Integer uid);

    @Update("update db_user set can_borrow = #{count} where uid = #{uid}")
    Integer updateBookCount(Integer uid, Integer count);

}
