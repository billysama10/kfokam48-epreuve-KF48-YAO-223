package fr.kfokam48.presences.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import fr.kfokam48.presences.domain.Etudiant;
import fr.kfokam48.presences.domain.Promotion;
import fr.kfokam48.presences.domain.SessionCours;
import fr.kfokam48.presences.repository.EtudiantRepository;
import fr.kfokam48.presences.repository.PromotionRepository;
import fr.kfokam48.presences.repository.SessionCoursRepository;

/**
 * Test d'intégration de l'opération imposée POST /api/presences, sur H2 en mémoire.
 * Rejoue les quatre branches du diagramme D3 avec les codes HTTP du contrat.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class PresenceControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PromotionRepository promotions;

    @Autowired
    private EtudiantRepository etudiants;

    @Autowired
    private SessionCoursRepository sessions;

    private Long etudiantId;

    @BeforeEach
    void donnees() {
        Promotion promotion = promotions.save(new Promotion("Promo IT"));
        etudiantId = etudiants.save(new Etudiant("Awa", promotion)).getId();
        LocalDateTime maintenant = LocalDateTime.now(ZoneOffset.UTC);
        sessions.save(new SessionCours("Session ouverte", promotion, "OUVERT", maintenant));
        sessions.save(new SessionCours("Session passée", promotion, "EXPIRE", maintenant.minusMinutes(20)));
    }

    private ResultActions marquer(String code) throws Exception {
        return mockMvc.perform(post("/api/presences").contentType(MediaType.APPLICATION_JSON)
                .content("{\"code\":\"" + code + "\",\"etudiantId\":" + etudiantId + "}"));
    }

    @Test
    void casNominal201AvecSourceEtudiant() throws Exception {
        marquer("OUVERT")
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.sessionId").isNumber())
                .andExpect(jsonPath("$.etudiantId").value(etudiantId))
                .andExpect(jsonPath("$.source").value("ETUDIANT"));
    }

    @Test
    void dejaPresent409() throws Exception {
        marquer("OUVERT").andExpect(status().isCreated());

        marquer("OUVERT")
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("DEJA_PRESENT"))
                .andExpect(jsonPath("$.message").isNotEmpty());
    }

    @Test
    void codeExpire410() throws Exception {
        marquer("EXPIRE")
                .andExpect(status().isGone())
                .andExpect(jsonPath("$.code").value("CODE_EXPIRE"))
                .andExpect(jsonPath("$.message").value("Le code de présence a expiré."));
    }

    @Test
    void codeInconnu400() throws Exception {
        marquer("ZZZZZZ")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("CODE_INCONNU"))
                .andExpect(jsonPath("$.message").isNotEmpty());
    }
}
