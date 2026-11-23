package it.echoeswar.management.deck.controller;

import it.echoeswar.management.deck.enumerated.DeckStatus;
import it.echoeswar.management.deck.enumerated.DeckStatusFilter;
import it.echoeswar.management.deck.payload.response.DeckSummaryResponse;
import it.echoeswar.management.deck.service.DeckService;
import it.echoeswar.management.security.jwt.converter.CustomJwtAuthenticationConverter;
import it.echoeswar.management.security.userdetails.CustomUserDetails;
import it.echoeswar.management.security.userdetails.CustomUserDetailsService;
import it.echoeswar.management.user.entity.Role;
import it.echoeswar.management.user.entity.User;
import it.echoeswar.management.user.enumerated.AccountStatus;
import it.echoeswar.management.user.enumerated.RoleName;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.test.context.TestSecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.testSecurityContext;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(DeckController.class)
@AutoConfigureMockMvc
@Import(DeckControllerValidationTest.AuthenticationPrincipalResolverConfiguration.class)
class DeckControllerValidationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private DeckService deckService;

    @MockitoBean
    private CustomJwtAuthenticationConverter customJwtAuthenticationConverter;

    @MockitoBean
    private CustomUserDetailsService customUserDetailsService;

    @AfterEach
    void clearSecurityContext() {
        TestSecurityContextHolder.clearContext();
    }

    @Test
    void getUserDecksDefaultsToReadyAndOmitsCards() throws Exception {
        CustomUserDetails userDetails = authenticatedUser();
        DeckSummaryResponse deck = new DeckSummaryResponse(
                42L,
                "Fiamme e ombre",
                DeckStatus.READY
        );
        when(deckService.getUserDecks(eq(userDetails), eq(DeckStatusFilter.READY)))
                .thenReturn(List.of(deck));

        TestSecurityContextHolder.setAuthentication(authenticationFor(userDetails));
        mockMvc.perform(get("/api/v1/private/decks")
                        .with(testSecurityContext()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].id").value(42))
                .andExpect(jsonPath("$.data[0].status").value("READY"))
                .andExpect(jsonPath("$.data[0].cards").doesNotExist());

        verify(deckService).getUserDecks(eq(userDetails), eq(DeckStatusFilter.READY));
    }

    @Test
    void incompleteCreateDeckRequestReturnsStandardValidationResponse() throws Exception {
        CustomUserDetails userDetails = authenticatedUser();
        TestSecurityContextHolder.setAuthentication(authenticationFor(userDetails));
        mockMvc.perform(post("/api/v1/private/decks")
                        .with(testSecurityContext())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("Some fields are invalid."))
                .andExpect(jsonPath("$.errors.name").exists())
                .andExpect(jsonPath("$.errors.factionIds").exists())
                .andExpect(jsonPath("$.errors.cards").exists());

        verifyNoInteractions(deckService);
    }

    private static CustomUserDetails authenticatedUser() {
        User user = User.builder()
                .id(UUID.randomUUID())
                .email("deck-controller-test@example.com")
                .accountStatus(AccountStatus.ACTIVE)
                .role(Role.builder().roleName(RoleName.ROLE_USER).build())
                .build();
        return CustomUserDetails.forJwtAuthentication(user);
    }

    private static UsernamePasswordAuthenticationToken authenticationFor(
            CustomUserDetails userDetails
    ) {
        return new UsernamePasswordAuthenticationToken(
                userDetails,
                null,
                userDetails.getAuthorities()
        );
    }

    @TestConfiguration(proxyBeanMethods = false)
    static class AuthenticationPrincipalResolverConfiguration implements WebMvcConfigurer {

        @Override
        public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
            resolvers.add(new SessionAuthenticationPrincipalArgumentResolver());
        }
    }

    private static final class SessionAuthenticationPrincipalArgumentResolver
            implements HandlerMethodArgumentResolver {

        @Override
        public boolean supportsParameter(MethodParameter parameter) {
            return parameter.hasParameterAnnotation(AuthenticationPrincipal.class)
                    && CustomUserDetails.class.isAssignableFrom(parameter.getParameterType());
        }

        @Override
        public Object resolveArgument(
                MethodParameter parameter,
                ModelAndViewContainer modelAndViewContainer,
                NativeWebRequest webRequest,
                WebDataBinderFactory webDataBinderFactory
        ) {
            HttpServletRequest request = webRequest.getNativeRequest(HttpServletRequest.class);
            if (request == null || request.getSession(false) == null) {
                return null;
            }

            SecurityContext securityContext = (SecurityContext) request.getSession(false).getAttribute(
                    HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY
            );
            return securityContext == null ? null : securityContext.getAuthentication().getPrincipal();
        }
    }
}
