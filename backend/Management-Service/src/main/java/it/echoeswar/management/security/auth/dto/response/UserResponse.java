package it.echoeswar.management.security.auth.dto.response;

import it.echoeswar.management.user.entity.User;
import it.echoeswar.management.user.enumerated.AccountStatus;
import it.echoeswar.management.user.enumerated.RoleName;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;

public record UserResponse(
        UUID id,
        String username,
        String email,
        RoleName role,
        AccountStatus accountStatus,
        Long selectedAvatarId,
        LocalDateTime createdAt
) {

    public static UserResponse toUserResponse(User user) {
        return new UserResponse(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getRole().getRoleName(),
                user.getAccountStatus(),
                user.getSelectedAvatar() == null
                        ? null
                        : user.getSelectedAvatar().getId(),
                user.getCreatedAt()
        );
    }
}
