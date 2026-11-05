package it.echoeswar.management.security.userdetails;

import com.fasterxml.jackson.annotation.JsonIgnore;
import it.echoeswar.management.user.entity.Role;
import it.echoeswar.management.user.entity.User;
import it.echoeswar.management.user.enumerated.AccountStatus;
import it.echoeswar.management.user.enumerated.RoleName;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Immutable security snapshot representing an authenticated application user.
 *
 * <p>This class stores only security data and never retains a JPA entity.</p>
 */
public final class CustomUserDetails implements UserDetails {

    private final UUID id;
    private final String email;
    private final String passwordHash;
    private final AccountStatus accountStatus;
    private final boolean deleted;
    private final List<GrantedAuthority> authorities;

    private CustomUserDetails(
            UUID id,
            String email,
            String passwordHash,
            AccountStatus accountStatus,
            boolean deleted,
            List<GrantedAuthority> authorities
    ) {
        this.id = Objects.requireNonNull(id, "id must not be null");
        this.email = Objects.requireNonNull(email, "email must not be null");
        this.passwordHash = Objects.requireNonNull(passwordHash, "passwordHash must not be null");
        this.accountStatus = Objects.requireNonNull(accountStatus, "accountStatus must not be null");
        this.deleted = deleted;
        this.authorities = List.copyOf(
                Objects.requireNonNull(authorities, "authorities must not be null")
        );
    }

    /**
     * Builds a principal for password-based authentication.
     *
     * <p>The password hash is included because DaoAuthenticationProvider needs it for BCrypt verification.</p>
     */
    public static CustomUserDetails forPasswordAuthentication(User user) {
        Objects.requireNonNull(user, "user must not be null");

        Role role = Objects.requireNonNull(user.getRole(), "user.role must not be null");
        RoleName roleName = Objects.requireNonNull(
                role.getRoleName(),
                "user.role.roleName must not be null"
        );

        return new CustomUserDetails(
                user.getId(),
                user.getEmail(),
                user.getPassword(),
                user.getAccountStatus(),
                user.getDeletedAt() != null,
                List.of(new SimpleGrantedAuthority(roleName.name()))
        );
    }

    public UUID getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public AccountStatus getAccountStatus() {
        return accountStatus;
    }

    public boolean isDeleted() {
        return deleted;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    @JsonIgnore
    public String getPassword() {
        return passwordHash;
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return accountStatus != AccountStatus.BANNED;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return accountStatus == AccountStatus.ACTIVE && !deleted;
    }
}