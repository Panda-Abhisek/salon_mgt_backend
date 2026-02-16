package com.panda.salon_mgt_backend.services.impl;

import com.panda.salon_mgt_backend.models.User;
import com.panda.salon_mgt_backend.payloads.UserResponse;
import com.panda.salon_mgt_backend.repositories.UserRepository;
import com.panda.salon_mgt_backend.services.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public User getCurrentUserEntity(Authentication authentication) {
        String email = authentication.getName();

        return userRepository.findByEmailWithRoles(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getCurrentUser(Authentication authentication) {
        User user = getCurrentUserEntity(authentication);

        return new UserResponse(
                user.getUserId(),
                user.getUserName(),
                user.getEmail(),
                user.isEnabled(),
                user.getRoles()
                        .stream()
                        .map(r -> r.getRoleName().name())
                        .collect(Collectors.toSet())
        );
    }
}
