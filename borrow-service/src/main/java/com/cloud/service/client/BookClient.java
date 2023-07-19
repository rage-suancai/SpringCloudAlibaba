package com.cloud.service.client;

import com.entity.Book;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(value = "book-service")
public interface BookClient {

    @GetMapping("/api/book/{bid}")
    Book getBookById(@PathVariable("bid") Integer bid);

    @GetMapping("/api/book/remain/{bid}")
    int bookRemain(@PathVariable("bid") int bid);

    @GetMapping("/api/book/borrow/{bid}")
    boolean bookBorrow(@PathVariable("bid") int bid);

}
