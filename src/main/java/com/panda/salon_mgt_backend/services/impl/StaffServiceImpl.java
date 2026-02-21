package com.panda.salon_mgt_backend.services.impl;

import com.panda.salon_mgt_backend.exceptions.AlreadyExistsException;
import com.panda.salon_mgt_backend.exceptions.CanNotException;
import com.panda.salon_mgt_backend.exceptions.DeactivateException;
import com.panda.salon_mgt_backend.exceptions.ResourceNotFoundException;
import com.panda.salon_mgt_backend.models.*;
import com.panda.salon_mgt_backend.payloads.AssignServicesRequest;
import com.panda.salon_mgt_backend.payloads.ServiceResponse;
import com.panda.salon_mgt_backend.payloads.StaffCreateRequest;
import com.panda.salon_mgt_backend.payloads.StaffResponse;
import com.panda.salon_mgt_backend.repositories.RoleRepository;
import com.panda.salon_mgt_backend.repositories.ServicesRepository;
import com.panda.salon_mgt_backend.repositories.UserRepository;
import com.panda.salon_mgt_backend.services.SalonService;
import com.panda.salon_mgt_backend.services.StaffService;
import com.panda.salon_mgt_backend.utils.TenantContext;
import com.panda.salon_mgt_backend.utils.TenantGuard;
import com.panda.salon_mgt_backend.utils.subscription.PlanGuard;
import com.panda.salon_mgt_backend.utils.subscription.PlanLimits;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class StaffServiceImpl implements StaffService {

    private final UserRepository userRepository;
    private final SalonService salonService;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final ServicesRepository servicesRepository;
    private final TenantContext tenantContext;
    private final TenantGuard tenantGuard;
    private final PlanGuard planGuard;

    @Override
    @Transactional(readOnly = true)
    public List<StaffResponse> getMyStaff(Authentication auth) {
//     Salon salon = salonService.getMySalonEntity(auth);
        Salon salon = tenantContext.getSalon(auth);

        return userRepository
                .findStaffBySalonWithRolesAndServices(salon)
                .stream()
                .map(this::map)   // ✅ single mapping function
                .toList();
    }

    @Override
    public StaffResponse addStaff(
            StaffCreateRequest request,
            Authentication auth
    ) {
//        Salon salon = salonService.getMySalonEntity(auth);
        Salon salon = tenantContext.getSalon(auth);

        if (userRepository.existsByEmail(request.email())) {
            throw new AlreadyExistsException("User with this email already exists");
        }

        Role staffRole = roleRepository.findByRoleName(AppRole.ROLE_STAFF)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found"));
        Role userRole = roleRepository.findByRoleName(AppRole.ROLE_USER)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found"));

        User staff = new User();
        staff.setUserName(request.name());
        staff.setEmail(request.email());
        staff.setPassword(passwordEncoder.encode(request.password()));
        staff.setEnabled(true);
        staff.setStaffSalon(salon);
        staff.setRoles(Set.of(userRole, staffRole));

        if (planGuard.isFree(auth)) {
//            Salon salon = tenantContext.getSalon(auth);
            long count = userRepository.countStaffBySalon(salon);

            if (count >= PlanLimits.FREE_MAX_STAFF) {
                throw new CanNotException("Free plan allows max 2 staff. Upgrade required.");
            }
        }

        User saved = userRepository.save(staff);

        return map(saved);
    }

    private User getStaff(Long id, Salon salon) {
        return userRepository.findByUserIdAndStaffSalon(id, salon)
                .orElseThrow(() -> new ResourceNotFoundException("Staff not found"));
    }

    @Override
    @Transactional
    public StaffResponse deactivateStaff(Long id, Authentication auth) {
//        Salon salon = salonService.getMySalonEntity(auth);
        Salon salon = tenantContext.getSalon(auth);
        User staff = getStaff(id, salon);

        // 1️⃣ deactivate staff
        staff.setEnabled(false);

        // 2️⃣ 🔥 auto-unassign from all services
        staff.getServices().clear();

        // 3️⃣ no explicit save needed if User is managed
        return map(staff);
    }

    @Override
    public StaffResponse reactivateStaff(Long id, Authentication auth) {
//        Salon salon = salonService.getMySalonEntity(auth);
        Salon salon = tenantContext.getSalon(auth);
        User staff = getStaff(id, salon);

        staff.setEnabled(true);
        return map(staff);
    }

    @Transactional(readOnly = true)
    @Override
    public List<ServiceResponse> getServicesForStaff(
            Long staffId,
            Authentication auth
    ) {
//        Salon salon = salonService.getMySalonEntity(auth);
        Salon salon = tenantContext.getSalon(auth);

        User staff = userRepository.findByUserIdAndStaffSalon(staffId, salon)
                .orElseThrow(() -> new ResourceNotFoundException("Staff not found"));

        return staff.getServices()
                .stream()
                .map(s -> new ServiceResponse(
                        s.getServiceId(),
                        s.getServiceName(),
                        s.getServicePrice(),
                        s.getDurationMinutes(),
                        s.isActive()
                ))
                .toList();
    }

    @Override
    public StaffResponse assignServicesToStaff(
            Long staffId,
            AssignServicesRequest request,
            Authentication auth
    ) {
//        Salon salon = salonService.getMySalonEntity(auth);
        Salon salon = tenantContext.getSalon(auth);

        User staff = userRepository.findByIdWithRolesAndServices(staffId)
                .orElseThrow(() -> new ResourceNotFoundException("Staff not found"));

        tenantGuard.assertStaffBelongsToTenant(staff, auth);

        if (!staff.isEnabled()) {
            throw new DeactivateException("Inactive staff cannot be assigned services");
        }

//        // 🔐 salon ownership check
//        if (!staff.getStaffSalon().getSalonId().equals(salon.getSalonId())) {
//            throw new AccessDeniedException("Staff does not belong to your salon");
//        }

        // fetch valid services only
        Set<Services> services = servicesRepository
                .findAllById(request.serviceIds())
                .stream()
                .peek(s -> tenantGuard.assertServiceBelongsToTenant(s, auth))
                .collect(Collectors.toSet());
        staff.setServices(services);
        return map(staff);
    }

    private StaffResponse map(User staff) {
        return new StaffResponse(
                staff.getUserId(),
                staff.getUserName(),
                staff.getEmail(),
                staff.isEnabled(),
                staff.getRoles().stream()
                        .map(r -> r.getRoleName().name())
                        .collect(Collectors.toSet()),
                staff.getServices().stream()
                        .map(s -> new ServiceResponse(
                                s.getServiceId(),
                                s.getServiceName(),
                                s.getServicePrice(),
                                s.getDurationMinutes(),
                                s.isActive()
                        ))
                        .collect(Collectors.toSet())
        );
    }

}
