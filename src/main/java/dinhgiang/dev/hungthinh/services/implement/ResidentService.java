package dinhgiang.dev.hungthinh.services.implement;

import dinhgiang.dev.hungthinh.exceptions.UserMessageException;
import dinhgiang.dev.hungthinh.models.dtos.residents.ResidentCreateRequest;
import dinhgiang.dev.hungthinh.models.dtos.residents.ResidentGetResponse;
import dinhgiang.dev.hungthinh.models.dtos.residents.ResidentUpdateRequest;
import dinhgiang.dev.hungthinh.models.entities.bases.Resident;
import dinhgiang.dev.hungthinh.models.entities.enums.ContractStatus;
import dinhgiang.dev.hungthinh.models.entities.enums.RelationshipType;
import dinhgiang.dev.hungthinh.models.entities.enums.UserRole;
import dinhgiang.dev.hungthinh.models.entities.global.PageResponse;
import dinhgiang.dev.hungthinh.repositories.ApartmentRepository;
import dinhgiang.dev.hungthinh.repositories.ContractRepository;
import dinhgiang.dev.hungthinh.repositories.ResidentRepository;
import dinhgiang.dev.hungthinh.services.interfaces.IResidentService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import dinhgiang.dev.hungthinh.components.Auditable;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Lớp cài đặt (Implementation) cho IResidentService.
 * Quản lý cư dân: thông tin cá nhân, tài khoản đăng nhập và liên kết với căn hộ.
 *
 * @author dinhgiang1
 * @version 1.0
 * @since 2026-05-10
 */
@Service
@RequiredArgsConstructor
public class ResidentService implements IResidentService {
    private final PasswordEncoder passwordEncoder;
    private final ResidentRepository residentRepository;
    private final ApartmentRepository apartmentRepository;
    private final ContractRepository contractRepository;

    public PageResponse<ResidentGetResponse> getAllResident(int page, int size, RelationshipType relationshipType, Boolean hasApartment, String sortBy, String direction, String keyword) {
        assertCurrentUserIsAdmin();

        Sort sort = direction.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        PageRequest pageRequest = PageRequest.of(page, size, sort);

        Specification<Resident> spec = Specification.allOf();
        if (relationshipType != null) {
            spec = spec.and((root, query, criteriaBuilder) ->
                    criteriaBuilder.equal(root.get("relationship"), relationshipType));
        }
        if (hasApartment != null) {
            spec = spec.and((root, query, cb) -> hasApartment
                    ? cb.isNotNull(root.get("apartment"))
                    : cb.isNull(root.get("apartment"))
            );
        }
        if (keyword != null && !keyword.isBlank()) {
            String likeKeyword = "%" + keyword.toLowerCase() + "%";
            spec = spec.and(((root, query, criteriaBuilder) ->
                    criteriaBuilder.or(
                            criteriaBuilder.like(criteriaBuilder.lower(root.get("fullName")), likeKeyword),
                            criteriaBuilder.like(criteriaBuilder.lower(root.get("phoneNumber")), likeKeyword),
                            criteriaBuilder.like(criteriaBuilder.lower(root.get("email")), likeKeyword)
                    )));
        }
        Page<Resident> residents = residentRepository.findAll(spec, pageRequest);

        List<ResidentGetResponse> content = residents.getContent().stream()
                .map(resident -> ResidentGetResponse.builder()
                        .id(resident.getId())
                        .userName(resident.getUsername())
                        .fullName(resident.getFullName())
                        .phoneNumber(resident.getPhoneNumber())
                        .email(resident.getEmail())
                        .relationship(resident.getRelationship())
                        .build())
                .toList();

        return new PageResponse<>(
                content,
                residents.getNumber() + 1,
                residents.getSize(),
                residents.getTotalElements(),
                residents.getTotalPages()
        );
    }

    public ResidentGetResponse getResidentById(Long residentId) {
        Resident resident = residentRepository.findById(residentId)
                .orElseThrow(() -> new UserMessageException("Cư dân không tồn tại"));

        assertCurrentUserCanAccess(resident);
        return toResidentGetResponse(resident);
    }

    public ResidentGetResponse getResidentByUsername(String username) {
        Resident resident = residentRepository.findByUsername(username)
                .orElseThrow(() -> new UserMessageException("Cư dân không tồn tại"));

        assertCurrentUserCanAccess(resident);
        return toResidentGetResponse(resident);
    }

    @Auditable(action = "CREATE", entityType = "RESIDENT")
    public Long createResident(ResidentCreateRequest apiRequest) {
        assertCurrentUserIsAdmin();

        if (apiRequest.getEmail() != null && !apiRequest.getEmail().isBlank()
                && residentRepository.existsByEmail(apiRequest.getEmail())) {
            throw new UserMessageException("Email đã được sử dụng");
        }
        if (apiRequest.getUserName() != null && !apiRequest.getUserName().isBlank()
                && residentRepository.existsByUsername(apiRequest.getUserName())) {
            throw new UserMessageException("Tên đăng nhập đã được sử dụng");
        }

        Resident resident = new Resident();
        if (hasText(apiRequest.getUserName())) {
            if (!hasText(apiRequest.getPassword())) {
                throw new UserMessageException("Mật khẩu không được để trống khi tạo tài khoản đăng nhập");
            }
            resident.setUsername(apiRequest.getUserName());
            resident.setPassword(passwordEncoder.encode(apiRequest.getPassword()));
        }
        resident.setRole(UserRole.RESIDENT);
        resident.setFullName(apiRequest.getFullName());
        resident.setPhoneNumber(apiRequest.getPhoneNumber());
        resident.setEmail(apiRequest.getEmail());
        resident.setRelationship(apiRequest.getRelationship());
        // Gán căn hộ nếu có
        if (apiRequest.getApartmentId() != null) {
            var apartment = apartmentRepository.findById(apiRequest.getApartmentId())
                    .orElseThrow(() -> new UserMessageException("Căn hộ không tồn tại"));
            resident.setApartment(apartment);
        }
        residentRepository.save(resident);
        return resident.getId();
    }

    @Auditable(action = "UPDATE", entityType = "RESIDENT")
    public Long updateResident(Long residentId, ResidentUpdateRequest apiRequest) {
        Resident resident = residentRepository.findById(residentId).orElseThrow(() -> new UserMessageException("Cư dân không tồn tại"));
        assertCurrentUserCanAccess(resident);

        if (apiRequest.getFullName() != null) {
            resident.setFullName(apiRequest.getFullName());
        }
        if (apiRequest.getPhoneNumber() != null) {
            resident.setPhoneNumber(apiRequest.getPhoneNumber());
        }
        if (apiRequest.getEmail() != null) {
            resident.setEmail(apiRequest.getEmail());
        }
        if (apiRequest.getRelationship() != null) {
            resident.setRelationship(apiRequest.getRelationship());
        }
        residentRepository.save(resident);
        return resident.getId();
    }

    @Auditable(action = "DELETE", entityType = "RESIDENT")
    public Void deleteResident(Long residentId) {
        assertCurrentUserIsAdmin();

        Resident resident = residentRepository.findById(residentId).orElseThrow(() -> new UserMessageException("Cư dân không tồn tại"));
        // Kiểm tra cư dân có đang là chủ hộ không
        if (resident.getApartment() != null) {
            Long ownerId = resident.getApartment().getOwnerId();
            if (ownerId != null && ownerId.equals(residentId)) {
                throw new UserMessageException("Không thể xóa cư dân đang là chủ hộ của căn hộ " + resident.getApartment().getApartmentNumber());
            }
        }
        // Kiểm tra cư dân có hợp đồng active không
        boolean hasActiveContract = contractRepository.existsByResidentIdAndContractStatus(residentId, ContractStatus.ACTIVE);
        if (hasActiveContract) {
            throw new UserMessageException("Không thể xóa cư dân còn hợp đồng đang hiệu lực");
        }
        residentRepository.delete(resident);
        return null;
    }

    private ResidentGetResponse toResidentGetResponse(Resident resident) {
        return ResidentGetResponse.builder()
                .id(resident.getId())
                .userName(resident.getUsername())
                .fullName(resident.getFullName())
                .phoneNumber(resident.getPhoneNumber())
                .email(resident.getEmail())
                .relationship(resident.getRelationship())
                .build();
    }

    private void assertCurrentUserCanAccess(Resident resident) {
        Authentication authentication = currentAuthentication();
        if (isAdmin(authentication)) {
            return;
        }

        String currentUsername = authentication.getName();
        if (resident.getUsername() == null || !resident.getUsername().equals(currentUsername)) {
            throw new UserMessageException("Bạn không có quyền truy cập cư dân này");
        }
    }

    private void assertCurrentUserIsAdmin() {
        if (!isAdmin(currentAuthentication())) {
            throw new UserMessageException("Chỉ quản trị viên mới có quyền thực hiện thao tác này");
        }
    }

    private Authentication currentAuthentication() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new UserMessageException("Bạn chưa đăng nhập");
        }
        return authentication;
    }

    private boolean isAdmin(Authentication authentication) {
        return authentication != null
                && authentication.getAuthorities().stream()
                .anyMatch(authority -> "ROLE_ADMIN".equals(authority.getAuthority()));
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
