package it.echoeswar.management.social.service;

import it.echoeswar.management.common.exception.ResourceConflictException;
import it.echoeswar.management.common.exception.ResourceNotFoundException;
import it.echoeswar.management.security.userdetails.CustomUserDetails;
import it.echoeswar.management.social.entity.Friendship;
import it.echoeswar.management.social.enumerated.FriendshipStatus;
import it.echoeswar.management.social.payload.response.FriendshipRequestResponse;
import it.echoeswar.management.social.repository.FriendshipRepository;
import it.echoeswar.management.user.entity.User;
import it.echoeswar.management.user.enumerated.AccountStatus;
import it.echoeswar.management.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Manages friendship-request operations between users.
 */
@Service
@RequiredArgsConstructor
public class FriendshipService {

    private final FriendshipRepository friendshipRepository;
    private final UserRepository userRepository;

    /**
     * Creates a friendship request with {@link FriendshipStatus#PENDING} status.
     *
     * <p>The requester identity is never received from the client. It is derived from the
     * {@link CustomUserDetails} associated with the authenticated token.</p>
     *
     * @param customUserDetails details of the authenticated user sending the request
     * @param recipientUsername username of the recipient user
     * @return the newly saved friendship request
     */
    @Transactional
    public FriendshipRequestResponse sendFriendRequest(
            CustomUserDetails customUserDetails,
            String recipientUsername
    ) {
        UUID requesterUserId = customUserDetails.getId();

        UUID recipientUserId = userRepository.findByUsernameAndAccountStatus(recipientUsername, AccountStatus.ACTIVE)
                .orElseThrow(() -> new ResourceNotFoundException("User", "username", recipientUsername)).getId();

        // A friendship relationship must involve two distinct users.
        if (requesterUserId.equals(recipientUserId)) {
            throw new IllegalArgumentException(
                    "You cannot send a friendship request to yourself."
            );
        }

        /*
         * The table requires user_a_id < user_b_id. Lexicographic ordering of the canonical
         * UUID representation matches PostgreSQL's byte-wise ordering, whereas UUID.compareTo()
         * compares signed long values.
         */
        boolean requesterIsCanonicalFirstUser = requesterUserId.toString()
                .compareTo(recipientUserId.toString()) < 0;
        UUID canonicalFirstUserId = requesterIsCanonicalFirstUser
                ? requesterUserId
                : recipientUserId;
        UUID canonicalSecondUserId = requesterIsCanonicalFirstUser
                ? recipientUserId
                : requesterUserId;

        /*
         * Locks are always acquired in the same canonical order. This prevents simultaneous
         * requests for the same pair from creating duplicates by reversing the user order.
         */
        User canonicalFirstUser = findUserForUpdate(canonicalFirstUserId);
        User canonicalSecondUser = findUserForUpdate(canonicalSecondUserId);
        User requester = requesterIsCanonicalFirstUser
                ? canonicalFirstUser
                : canonicalSecondUser;
        User recipient = requesterIsCanonicalFirstUser
                ? canonicalSecondUser
                : canonicalFirstUser;

        // An inactive or deleted recipient cannot receive requests.
        if (recipient.getAccountStatus() != AccountStatus.ACTIVE
                || recipient.getDeletedAt() != null) {
            throw new ResourceNotFoundException("User", "id", recipientUserId);
        }

        // The ordered pair also detects an already existing request in the opposite direction.
        if (friendshipRepository.existsByFirstUser_IdAndSecondUser_Id(
                canonicalFirstUserId,
                canonicalSecondUserId
        )) {
            throw new ResourceConflictException("A friendship relationship already exists between these users.");
        }

        Friendship friendship = Friendship.builder()
                .firstUser(canonicalFirstUser)
                .secondUser(canonicalSecondUser)
                // The requester remains correct even when it is the second user in canonical order.
                .requestedByUser(requester)
                // This flow only creates the request; acceptance is handled separately.
                .status(FriendshipStatus.PENDING)
                .build();

        try {
            /*
             * Flushing assigns the identifier and requestedAt timestamp before the response is
             * created. The database UNIQUE constraint remains an additional safeguard against
             * concurrent or external writes.
             */
            Friendship savedFriendship = friendshipRepository.saveAndFlush(friendship);

            return new FriendshipRequestResponse(
                    savedFriendship.getId(),
                    requesterUserId,
                    requester.getUsername(),
                    recipientUserId,
                    recipient.getUsername(),
                    savedFriendship.getStatus(),
                    savedFriendship.getRequestedAt()
            );
        } catch (DataIntegrityViolationException exception) {
            // Converts a uniqueness-constraint violation into the API's HTTP 409 response.
            throw new ResourceConflictException("A friendship relationship already exists between these users.");
        }
    }

    /**
     * Returns the pending requests received by the authenticated user, ordered from newest to oldest.
     *
     * @param customUserDetails details of the authenticated request recipient
     * @return the recipient's pending friendship requests
     */
    @Transactional(readOnly = true)
    public List<FriendshipRequestResponse> getReceivedFriendshipRequests(
            CustomUserDetails customUserDetails
    ) {
        UUID recipientUserId = customUserDetails.getId();

        List<Friendship> friendshipRequests = friendshipRepository.findReceivedRequestsByRecipientUserIdAndStatus(
                        recipientUserId,
                        FriendshipStatus.PENDING
                );

        if (friendshipRequests.isEmpty()) {
            return List.of();
        }

        User recipient = userRepository.findById(recipientUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", recipientUserId));

        return friendshipRequests
                .stream()
                .map(friendship -> FriendshipRequestResponse.from(friendship, recipient))
                .toList();
    }

    /**
     * Accepts a pending friendship request on behalf of its authenticated recipient.
     *
     * <p>Only the recipient may accept the request. The method transitions the request from
     * {@link FriendshipStatus#PENDING} to {@link FriendshipStatus#ACCEPTED}, records the acceptance
     * timestamp, and locks the friendship row to make concurrent transitions atomic.</p>
     *
     * @param customUserDetails details of the authenticated recipient
     * @param friendshipId identifier of the friendship request to accept
     * @return the accepted friendship request
     */
    @Transactional
    public FriendshipRequestResponse acceptFriendRequest(
            CustomUserDetails customUserDetails,
            Long friendshipId
    ) {
        Friendship friendship = friendshipRepository.findByIdForUpdateWithUsers(friendshipId)
                .orElseThrow(() -> new ResourceNotFoundException("Friendship request", "id", friendshipId));

        User recipient = recipientOf(friendship);
        if (!recipient.getId().equals(customUserDetails.getId())) {
            throw new AccessDeniedException("Only the recipient can accept this friendship request.");
        }
        if (friendship.getStatus() != FriendshipStatus.PENDING) {
            throw new ResourceConflictException("The friendship request is no longer pending.");
        }

        friendship.setStatus(FriendshipStatus.ACCEPTED);
        friendship.setAcceptedAt(LocalDateTime.now());
        return FriendshipRequestResponse.from(friendship, recipient);
    }

    /**
     * Rejects a pending friendship request on behalf of its authenticated recipient.
     *
     * <p>Only the recipient may reject the request. The method transitions the request from
     * {@link FriendshipStatus#PENDING} to {@link FriendshipStatus#REJECTED}, records the rejection
     * timestamp, and locks the friendship row to make concurrent transitions atomic.</p>
     *
     * @param customUserDetails details of the authenticated recipient
     * @param friendshipId identifier of the friendship request to reject
     * @return the rejected friendship request
     */
    @Transactional
    public FriendshipRequestResponse rejectFriendRequest(
            CustomUserDetails customUserDetails,
            Long friendshipId
    ) {
        Friendship friendship = friendshipRepository.findByIdForUpdateWithUsers(friendshipId)
                .orElseThrow(() -> new ResourceNotFoundException("Friendship request", "id", friendshipId));

        User recipient = recipientOf(friendship);
        if (!recipient.getId().equals(customUserDetails.getId())) {
            throw new AccessDeniedException("Only the recipient can reject this friendship request.");
        }
        if (friendship.getStatus() != FriendshipStatus.PENDING) {
            throw new ResourceConflictException("The friendship request is no longer pending.");
        }

        friendship.setStatus(FriendshipStatus.REJECTED);
        friendship.setRejectedAt(LocalDateTime.now());
        return FriendshipRequestResponse.from(friendship, recipient);
    }

    /**
     * Cancels a pending friendship request on behalf of the authenticated requester.
     *
     * <p>Only the user who sent the request may cancel it. The method transitions the request from
     * {@link FriendshipStatus#PENDING} to {@link FriendshipStatus#REMOVED}, records the removal
     * timestamp, and locks the friendship row to make concurrent transitions atomic.</p>
     *
     * @param customUserDetails details of the authenticated requester
     * @param friendshipId identifier of the friendship request to cancel
     * @return the cancelled friendship request
     */
    @Transactional
    public FriendshipRequestResponse cancelSentFriendRequest(
            CustomUserDetails customUserDetails,
            Long friendshipId
    ) {
        Friendship friendship = friendshipRepository.findByIdForUpdateWithUsers(friendshipId)
                .orElseThrow(() -> new ResourceNotFoundException("Friendship request", "id", friendshipId));

        User recipient = recipientOf(friendship);
        if (!friendship.getRequestedByUser().getId().equals(customUserDetails.getId())) {
            throw new AccessDeniedException("Only the requester can cancel this friendship request.");
        }
        if (friendship.getStatus() != FriendshipStatus.PENDING) {
            throw new ResourceConflictException("The friendship request is no longer pending.");
        }

        friendship.setStatus(FriendshipStatus.REMOVED);
        friendship.setRemovedAt(LocalDateTime.now());
        return FriendshipRequestResponse.from(friendship, recipient);
    }

    /**
     * Loads and temporarily locks a user while a friendship relationship is being created.
     *
     * @param userId identifier of the user to lock
     * @return the locked user
     */
    private User findUserForUpdate(UUID userId) {
        return userRepository.findByIdForUpdate(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
    }

    /**
     * Determines the request recipient from the canonical friendship pair.
     *
     * @param friendship friendship request whose recipient is required
     * @return the participant who did not send the request
     * @throws IllegalStateException if the recorded requester is not a friendship participant
     */
    private User recipientOf(Friendship friendship) {
        UUID requesterUserId = friendship.getRequestedByUser().getId();
        if (friendship.getFirstUser().getId().equals(requesterUserId)) {
            return friendship.getSecondUser();
        }
        if (friendship.getSecondUser().getId().equals(requesterUserId)) {
            return friendship.getFirstUser();
        }
        throw new IllegalStateException("The friendship requester must be one of the participants.");
    }
}
