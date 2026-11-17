package it.echoeswar.management.credit.controller;

import it.echoeswar.management.credit.service.CreditTransactionService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CreditController.class)
@AutoConfigureMockMvc(addFilters = false)
class CreditControllerValidationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CreditTransactionService creditTransactionService;

    @Test
    void invalidPageSizeReturnsStandardValidationResponse() throws Exception {
        mockMvc.perform(get("/api/v1/private/credits").param("pageSize", "0"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("Some fields are invalid."))
                .andExpect(jsonPath("$.errors.pageSize").exists());

        verifyNoInteractions(creditTransactionService);
    }
}
