package com.cloud.controller;

import com.cloud.service.BookService;
import com.entity.Book;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RefreshScope
@RestController
public class BookController {

    @Resource
    private BookService bookService;

    private int BookCount = 0;

    @GetMapping("/api/book/{bid}")
    public Book findBookById(@PathVariable Integer bid) {

        int count = BookCount++; System.err.println("调用图书服务" + count + "次");
        return bookService.getBookById(bid);

    }

    @GetMapping("/api/book/remain/{bid}")
    public Integer bookRemain(@PathVariable("bid") Integer bid) {
        return bookService.getRemain(bid);
    }

    @GetMapping("/api/book/borrow/{bid}")
    public boolean bookBorrow(@PathVariable("bid") Integer bid) {

        int remain = bookService.getRemain(bid);
        return bookService.setRemain(bid, remain - 1);

    }

}
