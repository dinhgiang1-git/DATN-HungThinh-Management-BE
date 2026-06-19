package dinhgiang.dev.hungthinh.services.implement;

import dinhgiang.dev.hungthinh.exceptions.UserMessageException;
import dinhgiang.dev.hungthinh.models.entities.bases.Apartment;
import dinhgiang.dev.hungthinh.models.entities.bases.Resident;
import dinhgiang.dev.hungthinh.repositories.ResidentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

/**
 * Lớp dịch vụ kiểm soát quyền truy cập.
 * Kiểm tra xem người dùng hiện tại có quyền thực hiện thao tác cụ thể hay không.
 *
 * @author dinhgiang1
 * @version 1.0
 * @since 2026-05-10
 */
@Service
@RequiredArgsConstructor
public class AccessControlService {
    private final ResidentRepository residentRepository;

    public boolean isAdmin() {
        return hasRole("ROLE_ADMIN");
    }

    public boolean isResident() {
        return hasRole("ROLE_RESIDENT");
    }

    public Resident currentResident() {
        Authentication authentication = currentAuthentication();
        return residentRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new UserMessageException("Cư dân không tồn tại"));
    }

    public Long currentResidentApartmentId() {
        Resident resident = currentResident();
        if (resident.getApartment() == null) {
            throw new UserMessageException("Cư dân chưa được gán căn hộ");
        }
        return resident.getApartment().getId();
    }

    public void assertResidentCanAccessApartment(Apartment apartment, String message) {
        if (!isResident()) {
            return;
        }
        if (apartment == null || apartment.getId() == null) {
            throw new UserMessageException(message);
        }
        assertResidentCanAccessApartment(apartment.getId(), message);
    }

    public void assertResidentCanAccessApartment(Long apartmentId, String message) {
        if (!isResident()) {
            return;
        }
        if (apartmentId == null || !currentResidentApartmentId().equals(apartmentId)) {
            throw new UserMessageException(message);
        }
    }

    public Long resolveApartmentScopeForResident(Long requestedApartmentId, String message) {
        if (!isResident()) {
            return requestedApartmentId;
        }

        Long currentApartmentId = currentResidentApartmentId();
        if (requestedApartmentId != null && !currentApartmentId.equals(requestedApartmentId)) {
            throw new UserMessageException(message);
        }
        return currentApartmentId;
    }

    private Authentication currentAuthentication() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new UserMessageException("Bạn chưa đăng nhập");
        }
        return authentication;
    }

    private boolean hasRole(String role) {
        Authentication authentication = currentAuthentication();
        return authentication.getAuthorities().stream()
                .anyMatch(authority -> role.equals(authority.getAuthority()));
    }
}
