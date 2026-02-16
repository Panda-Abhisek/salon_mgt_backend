package com.panda.salon_mgt_backend.repositories;

import com.panda.salon_mgt_backend.models.Salon;
import com.panda.salon_mgt_backend.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface SalonRepository extends JpaRepository<Salon, Long> {

    @Query("""
                select s from Salon s
            """)
    List<Salon> findAllPublic();


    Optional<Salon> findByOwner(User owner);

    boolean existsByOwner(User owner);
}
