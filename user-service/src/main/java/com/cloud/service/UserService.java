package com.cloud.service;

import com.entity.User;

public interface UserService {

    User getUserById(Integer uid);

    Integer getRemain(Integer uid);

    boolean setRemain(Integer uid, Integer count);

}
