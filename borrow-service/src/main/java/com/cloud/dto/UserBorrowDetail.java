package com.cloud.dto;

import com.entity.Book;
import com.entity.User;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class UserBorrowDetail {

    private User user;
    private List<Book> bookList;

}
