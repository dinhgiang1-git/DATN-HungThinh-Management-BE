package dinhgiang.dev.hungthinh.services.implement;

import dinhgiang.dev.hungthinh.exceptions.UserMessageException;
import dinhgiang.dev.hungthinh.models.dtos.users.UserCreateRequest;
import dinhgiang.dev.hungthinh.models.dtos.users.UserGetResponse;
import dinhgiang.dev.hungthinh.models.dtos.users.UserUpdateRequest;
import dinhgiang.dev.hungthinh.models.entities.bases.User;
import dinhgiang.dev.hungthinh.models.entities.enums.UserRole;
import dinhgiang.dev.hungthinh.models.entities.global.PageResponse;
import dinhgiang.dev.hungthinh.repositories.UserRepository;
import dinhgiang.dev.hungthinh.services.interfaces.IUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import dinhgiang.dev.hungthinh.components.Auditable;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Lớp cài đặt (Implementation) cho IUserService.
 * Quản lý tài khoản nội bộ (Admin, Technician).
 *
 * @author dinhgiang1
 * @version 1.0
 * @since 2026-05-10
 */
@Service
@RequiredArgsConstructor
public class UserService implements IUserService {
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    public PageResponse<UserGetResponse> getAllUsers(int page, int size, UserRole userRole, String sortBy, String direction, String keyword) {
        Sort sort = direction.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        PageRequest pageRequest = PageRequest.of(page, size, sort);

        Specification<User> spec = Specification.allOf();
        if (userRole != null) {
            spec = spec.and((root, query, criteriaBuilder) ->
                    criteriaBuilder.equal(root.get("userRole"), userRole));
        }
        if (keyword != null && !keyword.isBlank()) {
            String likeKeyword = "%" + keyword.toLowerCase() + "%";
            spec = spec.and((root, query, criteriaBuilder) ->
                    criteriaBuilder.or(
                            criteriaBuilder.like(criteriaBuilder.lower(root.get("fullName")), likeKeyword),
                            criteriaBuilder.like(criteriaBuilder.lower(root.get("phoneNumber")), likeKeyword),
                            criteriaBuilder.like(criteriaBuilder.lower(root.get("email")), likeKeyword)

                    ));
        }
        Page<User> users = userRepository.findAll(spec, pageRequest);

        List<UserGetResponse> content = users.getContent().stream()
                .map( user -> UserGetResponse.builder()
                        .id(user.getId())
                        .username(user.getUsername())
                        .fullName(user.getFullName())
                        .phoneNumber(user.getPhoneNumber())
                        .email(user.getEmail())
                        .userRole(user.getUserRole())
                        .build())
                .toList();
        return new PageResponse<>(
                content,
                users.getNumber() + 1,
                users.getSize(),
                users.getTotalElements(),
                users.getTotalPages()
        );
    }

    public UserGetResponse getUserByUsername(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UserMessageException("Người dùng không tồn tại"));
        return UserGetResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .fullName(user.getFullName())
                .phoneNumber(user.getPhoneNumber())
                .email(user.getEmail())
                .userRole(user.getUserRole())
                .build();
    }

    @Auditable(action = "CREATE", entityType = "USER")
    public Long createUser(UserCreateRequest apiRequest) {
        User user = new User();
        if (userRepository.existsByUsername(apiRequest.getUserName())) throw new UserMessageException("Tên đăng nhập đã được sử dụng");
        if (userRepository.existsByEmail(apiRequest.getEmail())) throw new UserMessageException("Email đã được sử dụng");
        user.setUsername(apiRequest.getUserName());
        user.setPassword(passwordEncoder.encode(apiRequest.getPassword()));
        user.setUserRole(apiRequest.getUserRole());
        user.setFullName(apiRequest.getFullName());
        user.setPhoneNumber(apiRequest.getPhoneNumber());
        user.setEmail(apiRequest.getEmail());
        userRepository.save(user);
        return user.getId();
    }

    @Auditable(action = "UPDATE", entityType = "USER")
    public Long updateUser(Long userId, UserUpdateRequest apiRequest) {
        User user = userRepository.findById(userId).orElseThrow(() -> new UserMessageException("Người dùng không tồn tại"));

        if (apiRequest.getPassword() != null && !apiRequest.getPassword().isBlank()) {
            user.setPassword(passwordEncoder.encode(apiRequest.getPassword()));
        }
        if (apiRequest.getUserRole() != null) {
            user.setUserRole(apiRequest.getUserRole());
        }
        if (apiRequest.getFullName() != null && !apiRequest.getFullName().isBlank()) {
            user.setFullName(apiRequest.getFullName());
        }
        if (apiRequest.getPhoneNumber() != null && !apiRequest.getPhoneNumber().isBlank()) {
            user.setPhoneNumber(apiRequest.getPhoneNumber());
        }
        if (apiRequest.getEmail() != null && !apiRequest.getEmail().isBlank()) {
            user.setEmail(apiRequest.getEmail());
        }

        userRepository.save(user);
        return user.getId();
    }

    @Auditable(action = "DELETE", entityType = "USER")
    public Void deleteUser(Long userId) {
        User user = userRepository.findById(userId).orElseThrow( () -> new UserMessageException("Người dùng không tồn tại"));
        userRepository.delete(user);
        return null;
    }
}
