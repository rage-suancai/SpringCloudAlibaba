package com.cloud.service.impl;

import com.cloud.mapper.BookMapper;
import com.cloud.service.BookService;
import com.entity.Book;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service("bookService")
public class BookServiceImpl implements BookService {

    @Resource
    private BookMapper bookMapper;

    @Override
    public Book getBookById(Integer bid) {
        return bookMapper.getBookById(bid);
    }

    @Override
    public Integer getRemain(Integer bid) {
        return bookMapper.getRemain(bid);
    }

    @Override
    public boolean setRemain(Integer bid, Integer count) {
        return bookMapper.getRemain(bid) > 0;
    }

}
