package it.echoeswar.management.social.service;

import it.echoeswar.management.security.userdetails.CustomUserDetails;
import it.echoeswar.management.social.entity.Friendship;
import it.echoeswar.management.social.enumerated.FriendshipStatus;
import it.echoeswar.management.social.payload.response.FriendshipRequestResponse;
import it.echoeswar.management.social.repository.FriendshipRepository;
import it.echoeswar.management.user.entity.Role;
import it.echoeswar.management.user.entity.User;
import it.echoeswar.management.user.enumerated.AccountStatus;
import it.echoeswar.management.user.enumerated.RoleName;
import it.echoeswar.management.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FriendshipServiceTest {

    @Mock
    private FriendshipRepository friendshipRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private FriendshipService friendshipService;

    @Test
    void sendFriendRequestIncludesRequesterAndRecipientUsernames() {
        UUID requesterUserId = UUID.fromString("00000000-0000-0000-0000-000000000001");
        UUID recipientUserId = UUID.fromString("00000000-0000-0000-0000-000000000002");
        LocalDateTime requestedAt = LocalDateTime.of(2026, 11, 23, 10, 0);
        User requester = activeUser(requesterUserId, "requester");
        User recipient = activeUser(recipientUserId, "recipient");
        Friendship savedFriendship = Friendship.builder()
                .id(99L)
                .status(FriendshipStatus.PENDING)
                .requestedAt(requestedAt)
                .build();

        when(userRepository.findByUsernameAndAccountStatus("recipient", AccountStatus.ACTIVE))
                .thenReturn(Optional.of(recipient));
        when(userRepository.findByIdForUpdate(requesterUserId)).thenReturn(Optional.of(requester));
        when(userRepository.findByIdForUpdate(recipientUserId)).thenReturn(Optional.of(recipient));
        when(friendshipRepository.saveAndFlush(any(Friendship.class))).thenReturn(savedFriendship);

        FriendshipRequestResponse response = friendshipService.sendFriendRequest(
                userDetails(requesterUserId),
                "recipient"
        );

        assertThat(response).isEqualTo(new FriendshipRequestResponse(
                99L,
                requesterUserId,
                "requester",
                recipientUserId,
                "recipient",
                FriendshipStatus.PENDING,
                requestedAt
        ));
    }

    @Test
    void getReceivedFriendshipRequestsReturnsPendingRequestsForAuthenticatedRecipient() {
        UUID recipientUserId = UUID.randomUUID();
        UUID requesterUserId = UUID.randomUUID();
        LocalDateTime requestedAt = LocalDateTime.of(2026, 11, 20, 14, 30);
        User requester = User.builder()
                .id(requesterUserId)
                .username("requester")
                .build();
        User recipient = User.builder()
                .id(recipientUserId)
                .username("recipient")
                .build();
        Friendship friendship = Friendship.builder()
                .id(42L)
                .requestedByUser(requester)
                .status(FriendshipStatus.PENDING)
                .requestedAt(requestedAt)
                .build();

        when(friendshipRepository.findReceivedRequestsByRecipientUserIdAndStatus(
                recipientUserId,
                FriendshipStatus.PENDING
        )).thenReturn(List.of(friendship));
        when(userRepository.findById(recipientUserId)).thenReturn(Optional.of(recipient));

        List<FriendshipRequestResponse> responses = friendshipService
                .getReceivedFriendshipRequests(userDetails(recipientUserId));

        assertThat(responses).containsExactly(new FriendshipRequestResponse(
                42L,
                requesterUserId,
                "requester",
                recipientUserId,
                "recipient",
                FriendshipStatus.PENDING,
                requestedAt
        ));
        verify(friendshipRepository).findReceivedRequestsByRecipientUserIdAndStatus(
                recipientUserId,
                FriendshipStatus.PENDING
        );
        verify(userRepository).findById(recipientUserId);
    }

    private static CustomUserDetails userDetails(UUID userId) {
        User user = activeUser(userId, "friendship-service-test");

        return CustomUserDetails.forJwtAuthentication(user);
    }

    private static User activeUser(UUID userId, String username) {
        return User.builder()
                .id(userId)
                .username(username)
                .email(username + "@example.com")
                .accountStatus(AccountStatus.ACTIVE)
                .role(Role.builder().roleName(RoleName.ROLE_USER).build())
                .build();
    }
}
