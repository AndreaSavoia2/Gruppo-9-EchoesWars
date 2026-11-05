package it.echoeswar.management.user.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import it.echoeswar.management.user.enumerated.AccountStatus;

import java.time.Instant;
import java.util.UUID;

/**
 * Modello persistente dell'account. Le informazioni sensibili restano nell'entita' e vengono
 * convertite in DTO dedicati prima di essere esposte dalle API.
 */
@Entity
@Table(name = "users")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "username", nullable = false, length = 50, unique = true)
    private String username;

    @Column(name = "email", nullable = false, length = 255, unique = true)
    private String email;

    // Contiene soltanto l'hash BCrypt; @JsonIgnore e i DTO impediscono esposizioni accidentali.
    @Column(name = "password", nullable = false, length = 255)
    @JsonIgnore
    private String password;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "account_status", nullable = false, length = 32)
    private AccountStatus accountStatus = AccountStatus.ACTIVE;

    @Column(name = "email_verified_at")
    private Instant emailVerifiedAt;

    @Column(name = "deleted_at")
    private Instant deletedAt;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Column(name = "last_login_at")
    private Instant lastLoginAt;

    @Column(name = "password_changed_at")
    private Instant passwordChangedAt;

        /*@JoinColumn(name = "avatar_id")
        @ManyToOne
    private Avatar avatar;*/

    @JoinColumn(name = "role_id", nullable = false)
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private Role role;

    public boolean canAuthenticate(boolean emailVerificationRequired) {
        // Tutti i requisiti vengono concentrati qui cosi' login, refresh e validazione JWT
        // applicano la stessa politica per account attivi, non eliminati e verificati se necessario.
        return accountStatus == AccountStatus.ACTIVE
                && deletedAt == null
                && (!emailVerificationRequired || emailVerifiedAt != null);
    }
}
