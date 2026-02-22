package com.panda.salon_mgt_backend.services.impl;

import com.panda.salon_mgt_backend.exceptions.AlreadyExistsException;
import com.panda.salon_mgt_backend.exceptions.ResourceNotFoundException;
import com.panda.salon_mgt_backend.models.*;
import com.panda.salon_mgt_backend.payloads.SalonCreateRequest;
import com.panda.salon_mgt_backend.payloads.SalonResponse;
import com.panda.salon_mgt_backend.repositories.PlanRepository;
import com.panda.salon_mgt_backend.repositories.RoleRepository;
import com.panda.salon_mgt_backend.repositories.SalonRepository;
import com.panda.salon_mgt_backend.repositories.SubscriptionRepository;
import com.panda.salon_mgt_backend.services.SalonService;
import com.panda.salon_mgt_backend.services.UserService;
import com.panda.salon_mgt_backend.utils.subscription.TrialPolicy;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class SalonServiceImpl implements SalonService {

    private final SalonRepository salonRepository;
    private final RoleRepository roleRepository;
    private final UserService userService;
    private final PlanRepository planRepository;
    private final SubscriptionRepository subscriptionRepository;

    @Override
    @Transactional
    public SalonResponse createSalon(SalonCreateRequest request, Authentication auth) {

        User user = userService.getCurrentUserEntity(auth);

        if (salonRepository.existsByOwner(user)) {
            throw new AlreadyExistsException("User already owns a salon");
        }

        // 1️⃣ Create salon
        Salon salon = new Salon();
        salon.setSalonName(request.salonName());
        salon.setSalonAddress(request.salonAddress());
        salon.setOwner(user);

        Salon savedSalon = salonRepository.save(salon);

        // ---- ROLE UPGRADE ----
        Role salonAdminRole = roleRepository.findByRoleName(AppRole.ROLE_SALON_ADMIN)
                .orElseThrow(() -> new ResourceNotFoundException("ROLE_SALON_ADMIN not found"));
        user.getRoles().add(salonAdminRole);

        // 2️⃣ Assign Trial plan automatically
        Plan trialPlan = planRepository.findByType(PlanType.PRO).orElseThrow();

        Instant now = Instant.now();

        Subscription trial = Subscription.builder()
                .salon(salon)
                .plan(trialPlan)
                .status(SubscriptionStatus.TRIAL)
                .startDate(now)
                .endDate(now.plus(TrialPolicy.TRIAL_DURATION))
                .build();

        subscriptionRepository.save(trial);

        return mapToResponse(savedSalon);
    }

    @Override
    @Transactional(readOnly = true)
    public Salon getMySalonEntity(Authentication auth) {

        User user = userService.getCurrentUserEntity(auth);

        return salonRepository.findByOwner(user)
                .orElseThrow(() -> new ResourceNotFoundException("Salon not found"));
    }

    @Override
    @Transactional(readOnly = true)
    public SalonResponse getMySalon(Authentication auth) {
        Salon salon = getMySalonEntity(auth);
        return mapToResponse(salon);
    }

    @Override
    public SalonResponse updateMySalon(SalonCreateRequest request, Authentication auth) {

        User user = userService.getCurrentUserEntity(auth);

        Salon salon = salonRepository.findByOwner(user)
                .orElseThrow(() -> new ResourceNotFoundException("Salon not found"));

        salon.setSalonName(request.salonName());
        salon.setSalonAddress(request.salonAddress());

        return mapToResponse(salonRepository.save(salon));
    }

    private SalonResponse mapToResponse(Salon salon) {
        return new SalonResponse(
                salon.getSalonId(),
                salon.getSalonName(),
                salon.getSalonAddress()
        );
    }
}
