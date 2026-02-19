package com.panda.salon_mgt_backend.services.impl;

import com.panda.salon_mgt_backend.exceptions.AlreadyExistsException;
import com.panda.salon_mgt_backend.exceptions.DeactivateException;
import com.panda.salon_mgt_backend.exceptions.ResourceNotFoundException;
import com.panda.salon_mgt_backend.models.Salon;
import com.panda.salon_mgt_backend.models.Services;
import com.panda.salon_mgt_backend.models.User;
import com.panda.salon_mgt_backend.payloads.ServiceCreateRequest;
import com.panda.salon_mgt_backend.payloads.ServiceResponse;
import com.panda.salon_mgt_backend.payloads.ServiceUpdateRequest;
import com.panda.salon_mgt_backend.payloads.StaffResponse;
import com.panda.salon_mgt_backend.repositories.SalonRepository;
import com.panda.salon_mgt_backend.repositories.ServicesRepository;
import com.panda.salon_mgt_backend.repositories.UserRepository;
import com.panda.salon_mgt_backend.services.ServicesService;
import com.panda.salon_mgt_backend.utils.TenantContext;
import com.panda.salon_mgt_backend.utils.TenantGuard;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class ServicesServiceImpl implements ServicesService {

    private final ServicesRepository servicesRepository;
    private final UserRepository userRepository;
    private final SalonRepository salonRepository;
    private final TenantContext tenantContext;
    private final TenantGuard tenantGuard;

    @Override
    @Transactional(readOnly = true)
    public List<ServiceResponse> getMyServices(Authentication authentication) {

        // 1. Resolve salon from authenticated user
        Salon salon = tenantContext.getSalon(authentication);

        // 2. Fetch services via DTO projection (N+1 safe)
        return servicesRepository.findServiceResponsesBySalon(salon);
    }

    @Override
    public ServiceResponse createService(
            ServiceCreateRequest request,
            Authentication authentication
    ) {
        // 1. Resolve salon (ownership enforced here)
        Salon salon = tenantContext.getSalon(authentication);

        // 2. Uniqueness check (per salon)
        if (servicesRepository.existsBySalonAndServiceName(salon, request.name())) {
            throw new AlreadyExistsException("Service with this name already exists");
        }

        // 3. Create entity
        Services service = new Services();
        service.setServiceName(request.name());
        service.setServicePrice(request.price());
        service.setDurationMinutes(request.durationMinutes());
        service.setActive(true);
        service.setSalon(salon);

        // 4. Persist
        Services saved = servicesRepository.save(service);

        // 5. Map to response
        return new ServiceResponse(
                saved.getServiceId(),
                saved.getServiceName(),
                saved.getServicePrice(),
                saved.getDurationMinutes(),
                saved.isActive()
        );
    }

    @Override
    public ServiceResponse updateService(
            Long serviceId,
            ServiceUpdateRequest request,
            Authentication authentication
    ) {
        Salon salon = tenantContext.getSalon(authentication);

        Services service = servicesRepository
                .findByServiceIdAndSalon(serviceId, salon)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Service not found")
                );

        if (!service.getServiceName().equals(request.name())
                && servicesRepository.existsBySalonAndServiceName(salon, request.name())) {
            throw new AlreadyExistsException("Service with this name already exists");
        }

        service.setServiceName(request.name());
        service.setServicePrice(request.price());
        service.setDurationMinutes(request.durationMinutes());
        service.setActive(request.active());

        Services saved = servicesRepository.save(service);
        return toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ServiceResponse> getActiveServicesForSalon(Long salonId) {

        Salon salon = salonRepository.findById(salonId)
                .orElseThrow(() -> new ResourceNotFoundException("Salon not found"));

        return servicesRepository
                .findBySalonAndActiveTrue(salon)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    @Override
    public List<StaffResponse> getPublicStaffForService(Long serviceId) {

        Services service = servicesRepository
                .findById(serviceId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Service not found")
                );

        if (!service.isActive()) {
            throw new ResourceNotFoundException("Service not available");
        }

        return service.getStaff()
                .stream()
                .filter(User::isEnabled)
                .map(u -> new StaffResponse(
                        u.getUserId(),
                        u.getUserName(),
                        u.getEmail(),
                        true,
                        u.getRoles().stream()
                                .map(r -> r.getRoleName().name())
                                .collect(Collectors.toSet()),
                        Set.of() // no need to expose services here
                ))
                .toList();
    }

    @Transactional
    @Override
    public ServiceResponse deactivateService(
            Long serviceId,
            Authentication authentication
    ) {
        // 1️⃣ Resolve salon (ownership enforced)
        Salon salon = tenantContext.getSalon(authentication);

        // 2️⃣ Fetch service WITH staff (needed for validation)
        Services service = servicesRepository
                .findByServiceIdAndSalonWithStaff(serviceId, salon)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Service not found")
                );

        // 3️⃣ Business rule: block if staff assigned
        if (!service.getStaff().isEmpty()) {
            throw new DeactivateException(
                    "Cannot deactivate service while staff is assigned"
            );
        }

        // 4️⃣ Idempotent deactivation
        if (!service.isActive()) {
            return toResponse(service);
        }

        // 5️⃣ Deactivate (entity is managed → auto-persist)
        service.setActive(false);

        return toResponse(service);
    }

    @Override
    public ServiceResponse reactivateService(
            Long serviceId,
            Authentication authentication
    ) {
        // 1. Resolve salon (ownership enforced)
        Salon salon = tenantContext.getSalon(authentication);

        // 2. Fetch service scoped to salon
        Services service = servicesRepository
                .findByServiceIdAndSalon(serviceId, salon)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Service not found")
                );

        // 3. Idempotent reactivation
        if (service.isActive()) {
            return toResponse(service);
        }

        service.setActive(true);
        Services saved = servicesRepository.save(service);

        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    @Override
    public List<StaffResponse> getStaffForService(
            Long serviceId,
            Authentication auth
    ) {
        Salon salon = tenantContext.getSalon(auth);

        Services service = servicesRepository
                .findByServiceIdAndSalon(serviceId, salon)
                .orElseThrow(() -> new ResourceNotFoundException("Service not found"));

        return service.getStaff()
                .stream()
                .map(u -> new StaffResponse(
                        u.getUserId(),
                        u.getUserName(),
                        u.getEmail(),
                        u.isEnabled(),
                        u.getRoles().stream()
                                .map(r -> r.getRoleName().name())
                                .collect(Collectors.toSet()),
                        u.getServices().stream()
                                .map(s -> new ServiceResponse(
                                        s.getServiceId(),
                                        s.getServiceName(),
                                        s.getServicePrice(),
                                        s.getDurationMinutes(),
                                        s.isActive()
                                ))
                                .collect(Collectors.toSet())
                ))
                .toList();
    }

//    @Transactional
//    @Override
//    public List<StaffResponse> assignStaffToService(
//            Long serviceId,
//            AssignStaffRequest request,
//            Authentication auth
//    ) {
//        Salon salon = salonService.getMySalonEntity(auth);
//
//        Services service = servicesRepository
//                .findByServiceIdAndSalon(serviceId, salon)
//                .orElseThrow(() -> new ResourceNotFoundException("Service not found"));
//
//        List<User> staff = userRepository.findAllById(request.staffIds());
//
//        staff.forEach(u -> {
//            if (!salon.equals(u.getStaffSalon())) {
//                throw new IllegalStateException("Staff does not belong to this salon");
//            }
//            if (!u.isEnabled()) {
//                throw new IllegalStateException("Inactive staff cannot be assigned");
//            }
//        });
//
//        service.getStaff().clear();
//        service.getStaff().addAll(staff);
//
//        // 🔥 RETURN UPDATED STATE
//        return staff.stream()
//                .map(this::toStaffResponse)
//                .toList();
//    }

//    private StaffResponse toStaffResponse(User user) {
//        return new StaffResponse(
//                user.getUserId(),
//                user.getUserName(),
//                user.getEmail(),
//                user.isEnabled(),
//                user.getRoles().stream()
//                        .map(r -> r.getRoleName().name())
//                        .collect(Collectors.toSet()),
//                user.getServices().stream()
//                        .map(s -> new ServiceResponse(
//                                s.getServiceId(),
//                                s.getServiceName(),
//                                s.getServicePrice(),
//                                s.getDurationMinutes(),
//                                s.isActive()
//                        ))
//                        .collect(Collectors.toSet())
//        );
//    }

    private ServiceResponse toResponse(Services service) {
        return new ServiceResponse(
                service.getServiceId(),
                service.getServiceName(),
                service.getServicePrice(),
                service.getDurationMinutes(),
                service.isActive()
        );
    }

}
