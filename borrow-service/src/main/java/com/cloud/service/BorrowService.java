package com.cloud.service;

import com.cloud.dto.UserBorrowDetail;

public interface BorrowService {

    UserBorrowDetail getUserBorrowDetailByUid(Integer uid);

    boolean doBorrow(Integer uid, Integer bid);

}
