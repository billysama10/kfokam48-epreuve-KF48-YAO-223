package fr.kfokam48.presences.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Duration;
import java.time.OffsetDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import fr.kfokam48.presences.domain.Promotion;
import fr.kfokam48.presences.repository.PromotionRepository;

/** EF2 : POST /api/sessions, opération imposée par le contrat. */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class SessionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper json;

    @Autowired
    private PromotionRepository promotions;

    private Long promotionId;

    @BeforeEach
    void promotion() {
        promotionId = promotions.save(new Promotion("Promo test")).getId();
    }

    @Test
    void ouvrirUneSessionRenvoie201AvecUnCodeQuiExpireDans15Minutes() throws Exception {
        String corps = mockMvc.perform(post("/api/sessions").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"titre\":\"Cours 1\",\"promotionId\":" + promotionId + "}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.code").value(org.hamcrest.Matchers.matchesPattern("[A-Z0-9]{6}")))
                .andReturn().getResponse().getContentAsString();

        JsonNode reponse = json.readTree(corps);
        OffsetDateTime ouverture = OffsetDateTime.parse(reponse.get("ouvertureAt").asText());
        OffsetDateTime expiration = OffsetDateTime.parse(reponse.get("expirationAt").asText());
        assertThat(Duration.between(ouverture, expiration)).isEqualTo(Duration.ofMinutes(15));
    }

    @Test
    void unTitreManquantRenvoie400AuFormatImpose() throws Exception {
        mockMvc.perform(post("/api/sessions").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"promotionId\":" + promotionId + "}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("REQUETE_INVALIDE"))
                .andExpect(jsonPath("$.message").isNotEmpty());
    }
}
