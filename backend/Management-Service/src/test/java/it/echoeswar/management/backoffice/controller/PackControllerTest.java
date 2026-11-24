package it.echoeswar.management.backoffice.controller;

import it.echoeswar.management.backoffice.service.BackofficeService;
import it.echoeswar.management.backoffice.payload.request.PackRequest;
import it.echoeswar.management.backoffice.payload.response.PackResponse;
import it.echoeswar.management.common.exception.ResourceConflictException;
import it.echoeswar.management.security.config.SecurityConfig;
import it.echoeswar.management.security.handler.RestAccessDeniedHandler;
import it.echoeswar.management.security.handler.RestAuthenticationEntryPoint;
import it.echoeswar.management.security.jwt.converter.CustomJwtAuthenticationConverter;
import it.echoeswar.management.security.session.config.RefreshTokenCookieProperties;
import it.echoeswar.management.security.session.service.AuthenticationSessionService;
import it.echoeswar.management.security.userdetails.CustomUserDetailsService;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import tools.jackson.databind.json.JsonMapper;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(BackofficeController.class)
@Import({SecurityConfig.class, PackControllerTest.WebSecurityTestConfig.class})
class PackControllerTest {

    /**
     * SecurityConfig does not declare @EnableWebSecurity (Spring Boot adds it
     * through auto-configuration, which is not active in a @WebMvcTest slice).
     * This provides HttpSecurity and AuthenticationConfiguration for the test only.
     */
    @TestConfiguration
    @EnableWebSecurity
    static class WebSecurityTestConfig {
    }

    private static final String URL = "/api/v1/private/packages";

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private JsonMapper jsonMapper;

    private MockMvc mockMvc;

    @MockitoBean
    private BackofficeService backofficeService;

    @MockitoBean
    private CustomUserDetailsService customUserDetailsService;

    @MockitoBean
    private AuthenticationSessionService authenticationSessionService;

    // Dependencies required by SecurityConfig
    @MockitoBean
    private RestAuthenticationEntryPoint restAuthenticationEntryPoint;

    @MockitoBean
    private RestAccessDeniedHandler restAccessDeniedHandler;

    @MockitoBean
    private CustomJwtAuthenticationConverter customJwtAuthenticationConverter;

    @MockitoBean
    private RefreshTokenCookieProperties refreshTokenCookieProperties;

    // Required by oauth2ResourceServer().jwt(); never used with @WithMockUser
    @MockitoBean
    private JwtDecoder jwtDecoder;

    @BeforeEach
    void setUp() throws Exception {
        // Apply the Spring Security filter chain and the test security context
        // (needed for @WithMockUser and csrf()).
        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity())
                .build();

        // Mocked handlers write nothing, so make them set the status
        // the real handlers would produce.
        doAnswer(invocation -> {
            HttpServletResponse response = invocation.getArgument(1);
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            return null;
        }).when(restAccessDeniedHandler).handle(any(), any(), any());

        doAnswer(invocation -> {
            HttpServletResponse response = invocation.getArgument(1);
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return null;
        }).when(restAuthenticationEntryPoint).commence(any(), any(), any());
    }

    private PackRequest validRequest() {
        return PackRequest.builder()
                .code("WEEKLY_001")
                .nameIt("Pacchetto settimanale")
                .nameEn("Weekly pack")
                .descriptionIt("Pacchetto disponibile ogni settimana")
                .descriptionEn("Available pack every week")
                .build();
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createPack_validRequest_returns201() throws Exception {
        PackResponse response = PackResponse.builder()
                .id(10L)
                .code("Weekly_001")
                .nameIt("Pacchetto settimanale")
                .nameEn("Weekly pack")
                .descriptionIt("Pacchetto disponibile ogni settimana")
                .descriptionEn("Available pack every week")
                .build();
        given(backofficeService.createPack(any(PackRequest.class))).willReturn(response);

        mockMvc.perform(post(URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(validRequest()))
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.id").value(10));

        verify(backofficeService).createPack(any(PackRequest.class));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createPack_withoutDescriptions_returns201() throws Exception {
        PackRequest request = PackRequest.builder()
                .code("WEEKLY_002")
                .nameIt("Pacchetto")
                .nameEn("Pack")
                .build();
        PackResponse response = PackResponse.builder()
                .id(11L)
                .code("Weekly_002")
                .nameIt("Pacchetto")
                .nameEn("Pack")
                .build();
        given(backofficeService.createPack(any(PackRequest.class))).willReturn(response);

        mockMvc.perform(post(URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(request))
                        .with(csrf()))
                .andExpect(status().isCreated());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createPack_invalidRequest_returns400() throws Exception {
        mockMvc.perform(post(URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}")
                        .with(csrf()))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(backofficeService);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createPack_codeTooLong_returns400() throws Exception {
        PackRequest request = PackRequest.builder()
                .code("A".repeat(101))
                .nameIt("Pacchetto")
                .nameEn("Pack")
                .build();

        mockMvc.perform(post(URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(request))
                        .with(csrf()))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(backofficeService);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createPack_duplicateCode_returns409() throws Exception {
        given(backofficeService.createPack(any(PackRequest.class)))
                .willThrow(new ResourceConflictException("Pack code already exists: WEEKLY_001"));

        mockMvc.perform(post(URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(validRequest()))
                        .with(csrf()))
                .andExpect(status().isConflict());
    }

    @Test
    @WithMockUser(roles = "USER")
    void createPack_nonAdminUser_returns403() throws Exception {
        mockMvc.perform(post(URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(validRequest()))
                        .with(csrf()))
                .andExpect(status().isForbidden());

        verifyNoInteractions(backofficeService);
    }
}