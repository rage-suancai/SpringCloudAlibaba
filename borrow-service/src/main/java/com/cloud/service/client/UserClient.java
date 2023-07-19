package com.cloud.service.client;

import com.entity.User;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(value = "user-service")
public interface UserClient {

    @GetMapping("/api/user/{uid}")
    User getUserById(@PathVariable("uid") Integer uid);

    @GetMapping("/api/user/remain/{uid}")
    int userRemain(@PathVariable("uid") int uid);

    @GetMapping("/api/user/borrow/{uid}")
    boolean userBorrow(@PathVariable("uid") int uid);

}
