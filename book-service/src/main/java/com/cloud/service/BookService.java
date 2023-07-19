package com.cloud.service;

import com.entity.Book;

public interface BookService {

    Book getBookById(Integer bid);

    Integer getRemain(Integer bid);

    boolean setRemain(Integer bid, Integer count);

}
