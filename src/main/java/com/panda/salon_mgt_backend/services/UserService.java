package com.panda.salon_mgt_backend.services;

import com.panda.salon_mgt_backend.models.User;
import com.panda.salon_mgt_backend.payloads.UserResponse;
import org.springframework.security.core.Authentication;

public interface UserService {

    User getCurrentUserEntity(Authentication authentication);

    UserResponse getCurrentUser(Authentication authentication);
}
