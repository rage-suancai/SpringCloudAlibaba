package com.cloud.service.impl;

import com.cloud.dto.UserBorrowDetail;
import com.cloud.mapper.BorrowMapper;
import com.cloud.service.BorrowService;
import com.cloud.service.client.BookClient;
import com.cloud.service.client.UserClient;
import com.entity.Book;
import com.entity.Borrow;
import com.entity.User;
import io.seata.core.context.RootContext;
import io.seata.spring.annotation.GlobalTransactional;
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

    // @SentinelResource("getBorrow")
    // @SentinelResource(value = "getBorrow", blockHandler = "blocked")
    @Override
    public UserBorrowDetail getUserBorrowDetailByUid(Integer uid) {

        List<Borrow> borrow = borrowMapper.getBorrowByUid(uid);

        User user = userClient.getUserById(uid);
        List<Book> bookList = borrow.stream()
                .map(b -> bookClient.getBookById(b.getBid()))
                .collect(Collectors.toList());
        return new UserBorrowDetail(user, bookList);

    }

    @GlobalTransactional
    @Override
    public boolean doBorrow(Integer uid, Integer bid) {

        System.err.println(RootContext.getXID());

        if (bookClient.bookRemain(bid) < 1) throw new RuntimeException("图书数量不足");
        if (userClient.userRemain(uid) < 1) throw new RuntimeException("用户借阅额度不足");
        if (!bookClient.bookBorrow(bid)) throw new RuntimeException("借阅图书时出现错误");
        if (borrowMapper.getBorrow(uid, bid) != null) throw new RuntimeException("此书籍已经被此用户借阅了");
        if (borrowMapper.addBorrow(uid, bid) <= 0) throw new RuntimeException("录入借阅信息时出现错误");
        if (!userClient.userBorrow(uid)) throw new RuntimeException("借阅时出现错误");
        return true;

    }

    /*public UserBorrowDetail blocked(Integer uid, BlockException e) {
        return new UserBorrowDetail(null, Collections.emptyList());
    }*/

}
