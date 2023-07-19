package com.cloud.controller;

import com.alibaba.fastjson.JSONObject;
import com.cloud.dto.UserBorrowDetail;
import com.cloud.service.BorrowService;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RefreshScope
@RestController
public class BorrowController2 {

    @Resource
    private BorrowService borrowService;

    @GetMapping("/borrow/{uid}")
    public UserBorrowDetail finUserBorrows(@PathVariable("uid") Integer uid) {
        return borrowService.getUserBorrowDetailByUid(uid);
    }

    @GetMapping("/borrow/task/{uid}/{bid}")
    public JSONObject borrow(@PathVariable("uid") Integer uid,
                             @PathVariable("bid") Integer bid) {

        borrowService.doBorrow(uid, bid);

        JSONObject object = new JSONObject();
        object.put("code", "200");
        object.put("success", false);
        object.put("message", "借阅成功");
        return object;

    }

}
