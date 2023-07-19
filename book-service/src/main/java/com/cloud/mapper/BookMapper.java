package com.cloud.mapper;

import com.entity.Book;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface BookMapper {

    @Select("select bid,title,`desc` from db_book where bid = #{bid}")
    Book getBookById(Integer bid);

    @Select("select count from db_book where bid = #{bid}")
    Integer getRemain(Integer bid);

    @Update("update db_book set count = #{count} where bid = #{bid}")
    Integer setRemain(Integer bid, Integer count);

}
