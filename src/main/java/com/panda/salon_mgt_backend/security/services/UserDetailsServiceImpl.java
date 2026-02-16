package com.panda.salon_mgt_backend.security.services;

import com.panda.salon_mgt_backend.models.User;
import com.panda.salon_mgt_backend.repositories.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    @Transactional
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByEmailWithRoles(username) //here username is email as we set in AuthTokenFilter
                .orElseThrow(() -> new UsernameNotFoundException("User Not Found with email " + username));
        return UserDetailsImpl.build(user);
    }
}
