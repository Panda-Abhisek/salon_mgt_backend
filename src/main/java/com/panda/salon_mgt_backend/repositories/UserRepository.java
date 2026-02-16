package com.panda.salon_mgt_backend.repositories;

import com.panda.salon_mgt_backend.models.Salon;
import com.panda.salon_mgt_backend.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    @Query("""
               select count(u)
               from User u
               join u.roles r
               where u.staffSalon = :salon
                 and r.roleName = com.panda.salon_mgt_backend.models.AppRole.ROLE_STAFF
            """)
    long countStaffBySalon(@Param("salon") Salon salon);

    Optional<User> findByEmail(String email);

    boolean existsByUserName(String username);

    boolean existsByEmail(String email);

    Optional<User> findByUserName(String username);

    @Query("""
                select u
                from User u
                left join fetch u.roles
                where u.userId = :id
            """)
    Optional<User> findByIdWithRoles(@Param("id") Long id);

    @Query("""
                select u
                from User u
                join fetch u.roles
                where u.email = :email
            """)
    Optional<User> findByEmailWithRoles(@Param("email") String email);

    @Query("""
                select u from User u
                join fetch u.roles
                where u.staffSalon = :salon
                  and exists (
                      select r from u.roles r
                      where r.roleName = com.panda.salon_mgt_backend.models.AppRole.ROLE_STAFF
                  )
            """)
    List<User> findStaffBySalon(@Param("salon") Salon salon);

    Optional<User> findByUserIdAndStaffSalon(Long userId, Salon staffSalon);

    @Query("""
                select u from User u
                left join fetch u.roles
                left join fetch u.services
                where u.userId = :id
            """)
    Optional<User> findByIdWithRolesAndServices(@Param("id") Long id);

    @Query("""
                select distinct u
                from User u
                left join fetch u.roles
                left join fetch u.services
                where u.staffSalon = :salon
            """)
    List<User> findStaffBySalonWithRolesAndServices(@Param("salon") Salon salon);

}
