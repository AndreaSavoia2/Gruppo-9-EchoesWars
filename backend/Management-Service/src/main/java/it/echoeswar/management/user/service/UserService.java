package it.echoeswar.management.user.service;

import it.echoeswar.management.common.exception.ResourceConflictException;
import it.echoeswar.management.common.exception.ResourceNotFoundException;
import it.echoeswar.management.common.utils.FormatterString;
import it.echoeswar.management.security.auth.dto.request.RegisterRequest;
import it.echoeswar.management.security.auth.dto.response.UserResponse;
import it.echoeswar.management.security.auth.password.PasswordPolicy;
import it.echoeswar.management.user.entity.Role;
import it.echoeswar.management.user.entity.User;
import it.echoeswar.management.user.enumerated.AccountStatus;
import it.echoeswar.management.user.enumerated.RoleName;
import it.echoeswar.management.user.repository.RoleRepository;
import it.echoeswar.management.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {

    private static final String ACCOUNT_CONFLICT_MESSAGE =
            "An account with the provided username or email already exists.";

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordPolicy passwordPolicy;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public UserResponse register(RegisterRequest request) {
        Objects.requireNonNull(request, "request must not be null");

        String normalizedEmail = FormatterString.normalizeEmail(request.email());

        passwordPolicy.validateForRegistration(request.password());

        if (userRepository.existsByUsername(request.username())
                || userRepository.existsByEmailIgnoreCase(normalizedEmail)) {
            throw new ResourceConflictException(ACCOUNT_CONFLICT_MESSAGE);
        }

        Role userRole = roleRepository.findByRoleName(RoleName.ROLE_USER)
                .orElseThrow(() -> new IllegalStateException(
                        "Default user role is not configured."
                ));

        User user = User.builder()
                .username(request.username())
                .email(normalizedEmail)
                .password(passwordEncoder.encode(request.password()))
                .accountStatus(AccountStatus.ACTIVE)
                .passwordChangedAt(Instant.now())
                .role(userRole)
                .build();

        try {
            User savedUser = userRepository.saveAndFlush(user);

            return UserResponse.toUserResponse(savedUser);
        } catch (DataIntegrityViolationException exception) {
            throw new ResourceConflictException(ACCOUNT_CONFLICT_MESSAGE);
        }
    }

    @PreAuthorize(
            "hasRole('ADMIN') or "
                    + "(hasRole('USER') and #p0 == authentication.principal.id)"
    )
    @Transactional(readOnly = true)
    public UserResponse getUserById(UUID userId) {
        Objects.requireNonNull(userId, "userId must not be null");

        return userRepository.findByIdWithRole(userId)
                .map(UserResponse::toUserResponse)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User",
                        "id",
                        userId
                ));
    }

}
