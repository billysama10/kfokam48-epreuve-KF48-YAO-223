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

/** EF4 : POST /api/exercices, opération imposée par le contrat. */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ExerciceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PromotionRepository promotions;

    @Autowired
    private EtudiantRepository etudiants;

    @Autowired
    private SessionCoursRepository sessions;

    private Long sessionId;
    private Long etudiantId;

    @BeforeEach
    void donnees() {
        Promotion promotion = promotions.save(new Promotion("Promo"));
        etudiantId = etudiants.save(new Etudiant("Awa", promotion)).getId();
        // Session dont le code est expiré : le dépôt reste possible (RG13)
        sessionId = sessions.save(new SessionCours("Cours", promotion, "EXO001",
                LocalDateTime.now(ZoneOffset.UTC).minusHours(3))).getId();
    }

    private ResultActions deposer(String lien) throws Exception {
        return mockMvc.perform(post("/api/exercices").contentType(MediaType.APPLICATION_JSON)
                .content("{\"sessionId\":" + sessionId + ",\"etudiantId\":" + etudiantId + ",\"lien\":\"" + lien + "\"}"));
    }

    @Test
    void depotApresLaFinDeSession201() throws Exception {
        deposer("https://github.com/awa/tp")
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.statut").value("DEPOSE"));
    }

    @Test
    void lienInvalide400() throws Exception {
        deposer("ftp://exemple/tp")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("LIEN_INVALIDE"));
        deposer("pas une adresse")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("LIEN_INVALIDE"));
    }

    @Test
    void secondDepot409() throws Exception {
        deposer("https://github.com/awa/tp").andExpect(status().isCreated());

        deposer("https://github.com/awa/tp-v2")
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("EXERCICE_DEJA_DEPOSE"));
    }
}
