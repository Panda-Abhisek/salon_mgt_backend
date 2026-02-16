package com.panda.salon_mgt_backend.utils;

import com.panda.salon_mgt_backend.exceptions.CanNotException;
import com.panda.salon_mgt_backend.models.User;
import com.panda.salon_mgt_backend.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AuthUtils {

    private final UserRepository userRepository;

    public User getCurrentUser(Authentication auth) {
        String username = auth.getName(); // usually email or username
        return userRepository.findByEmail(username)
                .orElseThrow(() -> new CanNotException("User not found"));
    }
}
