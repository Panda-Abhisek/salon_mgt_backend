package com.panda.salon_mgt_backend.security;

import com.panda.salon_mgt_backend.models.AppRole;
import com.panda.salon_mgt_backend.models.Role;
import com.panda.salon_mgt_backend.models.User;
import com.panda.salon_mgt_backend.repositories.RoleRepository;
import com.panda.salon_mgt_backend.repositories.UserRepository;
import com.panda.salon_mgt_backend.security.jwt.AuthEntryPointJwt;
import com.panda.salon_mgt_backend.security.jwt.AuthTokenFilter;
import com.panda.salon_mgt_backend.security.jwt.JwtAccessDeniedHandler;
import com.panda.salon_mgt_backend.security.services.UserDetailsServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.util.Set;

@Configuration
@RequiredArgsConstructor
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final AuthTokenFilter authenticationJwtTokenFilter;
    private final UserDetailsServiceImpl userDetailsService;
    private final AuthEntryPointJwt unauthorizedHandler;
    private final JwtAccessDeniedHandler accessDeniedHandler;

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> {})
                .anonymous(AbstractHttpConfigurer::disable)
                .exceptionHandling(exception -> exception.authenticationEntryPoint(unauthorizedHandler).accessDeniedHandler(accessDeniedHandler))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth ->
                        auth.requestMatchers("/api/auth/login", "/api/auth/register", "/api/auth/refresh").permitAll()
                                .requestMatchers(HttpMethod.GET, "/api/salons/me").authenticated()
                                .requestMatchers(HttpMethod.POST, "/api/salons").authenticated()
                                .requestMatchers("/api/salons/**").hasRole("SALON_ADMIN")
//                                .requestMatchers("/api/salons/staff/**").hasRole("SALON_ADMIN")
                                .requestMatchers("/api/admin/**").hasRole("SUPER_ADMIN")
                                .requestMatchers("/v3/api-docs/**").permitAll()
                                .requestMatchers("/api/public/**").permitAll()
                                .requestMatchers("/swagger-ui/**").permitAll()
                                .requestMatchers("/api/test/**").permitAll()
                                .requestMatchers("/images/**").permitAll()
                                .requestMatchers("/").permitAll()
                                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                                .anyRequest().authenticated()
                );
        http.addFilterBefore(authenticationJwtTokenFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    @Bean
    public DaoAuthenticationProvider daoAuthenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        return (web -> web.ignoring().requestMatchers("/v2/api-docs",
                "/configuration/ui",
                "/swagger-resources/**",
                "/configuration/security",
                "/swagger-ui.html",
                "/webjars/**"));
    }

    @Bean
    public CommandLineRunner initData(RoleRepository roleRepository, UserRepository userRepository, PasswordEncoder passwordEncoder) {
        return args -> {
            // Retrieve or create roles
            Role userRole = roleRepository.findByRoleName(AppRole.ROLE_USER)
                    .orElseGet(() -> {
                        Role newUserRole = new Role(AppRole.ROLE_USER);
                        return roleRepository.save(newUserRole);
                    });

            Role staffRole = roleRepository.findByRoleName(AppRole.ROLE_STAFF)
                    .orElseGet(() -> {
                        Role newStaffRole = new Role(AppRole.ROLE_STAFF);
                        return roleRepository.save(newStaffRole);
                    });

            Role salonOwnerRole = roleRepository.findByRoleName(AppRole.ROLE_SALON_ADMIN)
                    .orElseGet(() -> {
                        Role newSellerRole = new Role(AppRole.ROLE_SALON_ADMIN);
                        return roleRepository.save(newSellerRole);
                    });

            Role adminRole = roleRepository.findByRoleName(AppRole.ROLE_SUPER_ADMIN)
                    .orElseGet(() -> {
                        Role newAdminRole = new Role(AppRole.ROLE_SUPER_ADMIN);
                        return roleRepository.save(newAdminRole);
                    });

            Set<Role> userRoles = Set.of(userRole);
            Set<Role> salonOwnerRoles = Set.of(salonOwnerRole, userRole);
            Set<Role> adminRoles = Set.of(userRole, salonOwnerRole, adminRole);


            // Create users if not already present
            if (!userRepository.existsByUserName("user1")) {
                User user1 = new User("user1", "user1@example.com", passwordEncoder.encode("password1"), true);
                userRepository.save(user1);
            }

            if (!userRepository.existsByUserName("owner1")) {
                User owner1 = new User("owner1", "owner1@example.com", passwordEncoder.encode("password2"), true);
                userRepository.save(owner1);
            }

            if (!userRepository.existsByUserName("admin")) {
                User admin = new User("admin", "admin@example.com", passwordEncoder.encode("adminPass"), true);
                userRepository.save(admin);
            }

            // Update roles for existing users
            userRepository.findByUserName("user1").ifPresent(user -> {
                user.setRoles(userRoles);
                userRepository.save(user);
            });

            userRepository.findByUserName("owner1").ifPresent(owner -> {
                owner.setRoles(salonOwnerRoles);
                userRepository.save(owner);
            });

            userRepository.findByUserName("admin").ifPresent(admin -> {
                admin.setRoles(adminRoles);
                userRepository.save(admin);
            });
        };
    }

}
