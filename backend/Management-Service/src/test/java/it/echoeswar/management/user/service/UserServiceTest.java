package it.echoeswar.management.user.service;

import it.echoeswar.management.collection.repository.UserAvatarRepository;
import it.echoeswar.management.credit.repository.CreditWalletRepository;
import it.echoeswar.management.security.auth.password.PasswordPolicy;
import it.echoeswar.management.user.payload.response.UserSearchResponse;
import it.echoeswar.management.user.entity.User;
import it.echoeswar.management.user.enumerated.AccountStatus;
import it.echoeswar.management.user.repository.RoleRepository;
import it.echoeswar.management.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserAvatarRepository userAvatarRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private PasswordPolicy passwordPolicy;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private CreditWalletRepository creditWalletRepository;

    @InjectMocks
    private UserService userService;

    @Test
    void searchUsersByUsernameMapsMatches() {
        UUID userId = UUID.randomUUID();
        User user = User.builder()
                .id(userId)
                .username("andrea.savoia")
                .accountStatus(AccountStatus.ACTIVE)
                .build();
        when(userRepository.findAllVisibleByUsernameLike(
                eq("andrea"),
                eq(AccountStatus.ACTIVE)
        )).thenReturn(List.of(user));

        List<UserSearchResponse> response = userService.searchUsersByUsername(
                "  andrea  "
        );

        assertThat(response)
                .containsExactly(new UserSearchResponse(userId, "andrea.savoia"));

        verify(userRepository).findAllVisibleByUsernameLike(
                eq("andrea"),
                eq(AccountStatus.ACTIVE)
        );
    }
}
