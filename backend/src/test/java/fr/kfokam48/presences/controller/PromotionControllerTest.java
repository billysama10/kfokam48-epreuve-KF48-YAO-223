package fr.kfokam48.presences.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

/** EF1 : liste des étudiants d'une promotion. */
@SpringBootTest
@AutoConfigureMockMvc
class PromotionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void unePromotionInconnueRenvoie404() throws Exception {
        mockMvc.perform(get("/api/promotions/999/etudiants"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("PROMOTION_INCONNUE"));
    }
}
