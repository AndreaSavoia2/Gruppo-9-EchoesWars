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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Gestisce le operazioni relative alle relazioni di amicizia tra utenti.
 */
@Service
@RequiredArgsConstructor
public class FriendshipService {

    private final FriendshipRepository friendshipRepository;
    private final UserRepository userRepository;

    /**
     * Crea una richiesta di amicizia nello stato {@link FriendshipStatus#PENDING}.
     *
     * <p>L'identità del mittente non viene mai ricevuta dal client: viene ricavata
     * dal {@link CustomUserDetails} associato al token già autenticato.</p>
     *
     * @param customUserDetails dati dell'utente autenticato che invia la richiesta
     * @param recipientUserId identificativo dell'utente destinatario
     * @return i dati della richiesta appena salvata
     */
    @Transactional
    public FriendshipRequestResponse sendFriendRequest(
            CustomUserDetails customUserDetails,
            UUID recipientUserId
    ) {
        UUID requesterUserId = customUserDetails.getId();

        // Una relazione di amicizia deve coinvolgere due utenti distinti.
        if (requesterUserId.equals(recipientUserId)) {
            throw new IllegalArgumentException(
                    "You cannot send a friendship request to yourself."
            );
        }

        /*
         * La tabella impone user_a_id < user_b_id. L'ordinamento lessicografico
         * della rappresentazione UUID canonica coincide con l'ordinamento byte-wise
         * di PostgreSQL; UUID.compareTo(), invece, confronta long con segno.
         */
        boolean requesterIsUserA = requesterUserId.toString()
                .compareTo(recipientUserId.toString()) < 0;
        UUID userAId = requesterIsUserA ? requesterUserId : recipientUserId;
        UUID userBId = requesterIsUserA ? recipientUserId : requesterUserId;

        /*
         * I lock vengono acquisiti sempre nello stesso ordine canonico. In questo
         * modo due richieste simultanee tra la stessa coppia non possono creare
         * duplicati invertendo l'ordine degli utenti.
         */
        User userA = findUserForUpdate(userAId);
        User userB = findUserForUpdate(userBId);
        User requester = requesterIsUserA ? userA : userB;
        User recipient = requesterIsUserA ? userB : userA;

        // Un destinatario non più attivo o eliminato non può ricevere richieste.
        if (recipient.getAccountStatus() != AccountStatus.ACTIVE
                || recipient.getDeletedAt() != null) {
            throw new ResourceNotFoundException("User", "id", recipientUserId);
        }

        // La coppia ordinata permette di intercettare anche una richiesta inversa già esistente.
        if (friendshipRepository.existsByUserA_IdAndUserB_Id(userAId, userBId)) {
            throw new ResourceConflictException("A friendship relationship already exists between these users.");
        }

        Friendship friendship = Friendship.builder()
                .userA(userA)
                .userB(userB)
                // Il mittente resta corretto anche quando, nell'ordine canonico, è userB.
                .requestedByUser(requester)
                // La richiesta viene soltanto creata: l'accettazione sarà un flusso separato.
                .status(FriendshipStatus.PENDING)
                .build();

        try {
            /*
             * Il flush assegna l'id e requestedAt prima di costruire la risposta.
             * Il vincolo UNIQUE del database resta una protezione aggiuntiva in caso
             * di concorrenza o di scritture esterne all'applicazione.
             */
            Friendship savedFriendship = friendshipRepository.saveAndFlush(friendship);

            return new FriendshipRequestResponse(
                    savedFriendship.getId(),
                    requesterUserId,
                    recipientUserId,
                    savedFriendship.getStatus(),
                    savedFriendship.getRequestedAt()
            );
        } catch (DataIntegrityViolationException exception) {
            // Traduce il vincolo di unicità in una risposta HTTP 409 coerente con l'API.
            throw new ResourceConflictException("A friendship relationship already exists between these users.");
        }
    }

    /**
     * Carica e blocca temporaneamente un utente durante la creazione della relazione.
     */
    private User findUserForUpdate(UUID userId) {
        return userRepository.findByIdForUpdate(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
    }
}
