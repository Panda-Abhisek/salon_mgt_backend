package com.panda.salon_mgt_backend.repositories;

import com.panda.salon_mgt_backend.models.AppRole;
import com.panda.salon_mgt_backend.models.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Long> {
    Optional<Role> findByRoleName(AppRole appRole);
}
