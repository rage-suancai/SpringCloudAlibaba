package com.cloud.controller;

import com.cloud.dto.UserBorrowDetail;
import com.cloud.service.BorrowService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
public class BorrowController {

    @Resource
    private BorrowService borrowService;

    @GetMapping("/api/borrow/{uid}")
    public UserBorrowDetail getUserBorrowDetailByUid(@PathVariable("uid") Integer uid) {
        return borrowService.getUserBorrowDetailByUid(uid);
    }



}
