package com.cloud.service.impl;

import com.cloud.dto.UserBorrowDetail;
import com.cloud.mapper.BorrowMapper;
import com.cloud.service.client.BookClient;
import com.cloud.service.client.UserClient;
import com.cloud.service.BorrowService;
import com.entity.Book;
import com.entity.Borrow;
import com.entity.User;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

@Service("borrowService")
public class BorrowServiceImpl implements BorrowService {

    @Resource
    private BorrowMapper borrowMapper;
    @Resource
    private UserClient userClient;
    @Resource
    private BookClient bookClient;

    @Override
    public UserBorrowDetail getUserBorrowDetailByUid(Integer uid) {

        List<Borrow> borrow = borrowMapper.getBorrowByUid(uid);

        User user = userClient.getUserById(uid);
        List<Book> bookList = borrow.stream()
                .map(b -> bookClient.getBookById(b.getBid()))
                .collect(Collectors.toList());
        return new UserBorrowDetail(user, bookList);

    }



}
