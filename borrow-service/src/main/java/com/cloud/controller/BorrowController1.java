package com.cloud.controller;

import com.alibaba.csp.sentinel.annotation.SentinelResource;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.cloud.dto.UserBorrowDetail;
import com.cloud.service.BorrowService;
import com.entity.User;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.Collections;

@RefreshScope
@RestController
public class BorrowController1 {

    @Resource
    private BorrowService borrowService;
    private int BorrowCount = 0;

    /*@GetMapping("/api/borrow1/{uid}")
    public UserBorrowDetail getUserBorrowDetailByUid1(@PathVariable("uid") Integer uid) {

        int count = BorrowCount++; System.err.println("调用了借阅服务" + count + "次");
        return borrowService.getUserBorrowDetailByUid(uid);

    }*/

    /*@GetMapping("/api/borrow2/{uid}")
    public UserBorrowDetail getUserBorrowDetailByUid2(@PathVariable("uid") Integer uid) {

        int count = BorrowCount++; System.err.println("调用了借阅服务" + count + "次");
        return borrowService.getUserBorrowDetailByUid(uid);

    }*/

    /*@GetMapping("/api/borrow2/{uid}")
    public UserBorrowDetail getUserBorrowDetailByUid2(@PathVariable("uid") Integer uid)  {

        // TimeUnit.SECONDS.sleep(1); return borrowService.getUserBorrowDetailByUid(uid);
        throw new RuntimeException();

    }*/

    /*@GetMapping("/api/borrow2/{uid}")
    @SentinelResource(value = "findUserBorrows", blockHandler = "blocked")
    public UserBorrowDetail getUserBorrowDetailByUid2(@PathVariable("uid") Integer uid) {
        throw new RuntimeException();
    }
    public UserBorrowDetail blocked(Integer uid, BlockException e) {
        return new UserBorrowDetail(new User(), Collections.emptyList());
    }*/

    /*@GetMapping("/blocked")
    public JSONObject blocked() {

        JSONObject object = new JSONObject();

        object.put("code", 403);
        object.put("success", false);
        object.put("massage", "您的请求频率过快 请稍后再试");
        return object;

    }*/

    /*@GetMapping("/blocked")
    @SentinelResource(value = "blocked", fallback = "except", blockHandler = "block", exceptionsToIgnore = IOException.class)
    public String blocked() {
        throw new RuntimeException("Fuck World");
    }
    public String except(Throwable t) {
        return t.getMessage();
    }
    public String block(BlockException e) {
        return "被限流了";
    }*/

    /*@GetMapping("/blocked")
    @SentinelResource(value = "blocked")
    public String findUserBorrows(@RequestParam(value = "a", required = false) Integer a,
                                  @RequestParam(value = "b", required = false) Integer b,
                                  @RequestParam(value = "c", required = false) Integer c) {
        return "请求成功! a = " + a + ", b = " + b + ", c = " + c;
    }*/

}
