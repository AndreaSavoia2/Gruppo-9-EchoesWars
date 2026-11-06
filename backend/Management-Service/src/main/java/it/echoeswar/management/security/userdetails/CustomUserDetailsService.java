package it.echoeswar.management.security.userdetails;

import it.echoeswar.management.user.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Loads immutable security principals from persistent user accounts.
 */
@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Loads a user by email for DaoAuthenticationProvider.
     */
    @Override
    @Transactional(readOnly = true)
    public CustomUserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        return userRepository.findByEmailIgnoreCaseWithRole(email)
                .map(CustomUserDetails::forPasswordAuthentication)
                .orElseThrow(() -> new UsernameNotFoundException("Invalid email or password."));
    }

    /**
     * Loads a current user snapshot by JWT subject for bearer token authentication.
     */
    @Transactional(readOnly = true)
    public CustomUserDetails loadUserById(UUID userId) {
        return userRepository.findByIdWithRole(userId)
                .map(CustomUserDetails::forJwtAuthentication)
                .orElseThrow(() -> new UsernameNotFoundException(
                        "Authenticated user is no longer available."
                ));
    }
}
