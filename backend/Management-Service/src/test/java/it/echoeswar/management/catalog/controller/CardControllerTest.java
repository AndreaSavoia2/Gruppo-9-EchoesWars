package it.echoeswar.management.catalog.controller;

import it.echoeswar.management.backoffice.BackofficeController;
import it.echoeswar.management.backoffice.BackofficeService;
import it.echoeswar.management.catalog.payload.request.CardRequest;
import it.echoeswar.management.catalog.payload.response.CardResponse;
import it.echoeswar.management.security.session.service.AuthenticationSessionService;
import it.echoeswar.management.security.userdetails.CustomUserDetailsService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.json.JsonMapper;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(BackofficeController.class)
class CardControllerTest {

    private static final String URL = "/api/v1/cards";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JsonMapper jsonMapper;

    @MockitoBean
    private BackofficeService backofficeService;

    @MockitoBean
    private CustomUserDetailsService customUserDetailsService;

    @MockitoBean
    private AuthenticationSessionService authenticationSessionService;

    private CardRequest validRequest() {
        return new CardRequest(
                "DOMINION_001", "Guardiano", "Guardian",
                "Protegge gli alleati", "Protects allies",
                (byte) 8, (byte) 12, (byte) 6, (byte) 3,
                "https://cdn.echoeswar.it/cards/dominion-001.png",
                (byte) 2, (byte) 2, (byte) 2);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createCard_validRequest_returns201() throws Exception {
        CardResponse response = CardResponse.builder()
                .id(101L)
                .code("Dominion_001")
                .nameIt("Guardiano")
                .nameEn("Guardian")
                .attack((byte) 8)
                .defense((byte) 12)
                .cost((byte) 6)
                .echoesId((byte) 2)
                .schoolId((byte) 2)
                .tierLevel(2)
                .build();
        given(backofficeService.createCard(any(CardRequest.class))).willReturn(response);

        mockMvc.perform(post(URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(validRequest()))
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isCreated());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createCard_invalidRequest_returns400() throws Exception {
        mockMvc.perform(post(URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}")
                        .with(csrf()))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(backofficeService);
    }


}