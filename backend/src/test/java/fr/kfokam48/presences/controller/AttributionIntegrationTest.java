package fr.kfokam48.presences.controller;

import static org.assertj.core.api.Assertions.assertThat;
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
import fr.kfokam48.presences.domain.Relecture;
import fr.kfokam48.presences.domain.SessionCours;
import fr.kfokam48.presences.domain.StatutExercice;
import fr.kfokam48.presences.repository.EtudiantRepository;
import fr.kfokam48.presences.repository.ExerciceRepository;
import fr.kfokam48.presences.repository.PromotionRepository;
import fr.kfokam48.presences.repository.RelectureRepository;
import fr.kfokam48.presences.repository.SessionCoursRepository;

/** EF5 de bout en bout : attribution au dépôt, puis retentative à la présence suivante (RG9, RG10). */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class AttributionIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PromotionRepository promotions;

    @Autowired
    private EtudiantRepository etudiants;

    @Autowired
    private SessionCoursRepository sessions;

    @Autowired
    private ExerciceRepository exercices;

    @Autowired
    private RelectureRepository relectures;

    private Long sessionId;
    private Long awa;
    private Long boris;

    @BeforeEach
    void donnees() {
        Promotion promotion = promotions.save(new Promotion("Promo"));
        awa = etudiants.save(new Etudiant("Awa", promotion)).getId();
        boris = etudiants.save(new Etudiant("Boris", promotion)).getId();
        sessionId = sessions.save(new SessionCours("Cours", promotion, "ATTR01",
                LocalDateTime.now(ZoneOffset.UTC))).getId();
    }

    private ResultActions presence(Long etudiantId) throws Exception {
        return mockMvc.perform(post("/api/presences").contentType(MediaType.APPLICATION_JSON)
                .content("{\"code\":\"ATTR01\",\"etudiantId\":" + etudiantId + "}"));
    }

    private ResultActions depot(Long etudiantId) throws Exception {
        return mockMvc.perform(post("/api/exercices").contentType(MediaType.APPLICATION_JSON)
                .content("{\"sessionId\":" + sessionId + ",\"etudiantId\":" + etudiantId
                        + ",\"lien\":\"https://github.com/tp\"}"));
    }

    @Test
    void unAutrePresentEstAttribueDesLeDepot() throws Exception {
        presence(awa).andExpect(status().isCreated());
        presence(boris).andExpect(status().isCreated());

        depot(awa).andExpect(status().isCreated())
                .andExpect(jsonPath("$.statut").value("EN_ATTENTE_RELECTURE"));

        Relecture relecture = relectures.findAll().get(0);
        assertThat(relecture.getRelecteur().getId()).isEqualTo(boris);
    }

    @Test
    void sansAutrePresentLExerciceAttendPuisEstAttribueALaPresenceSuivante() throws Exception {
        presence(awa).andExpect(status().isCreated());
        String reponse = depot(awa).andExpect(status().isCreated())
                .andExpect(jsonPath("$.statut").value("DEPOSE"))
                .andReturn().getResponse().getContentAsString();
        Long exerciceId = Long.valueOf(reponse.replaceAll(".*\"id\":(\\d+).*", "$1"));

        presence(boris).andExpect(status().isCreated());

        assertThat(exercices.findById(exerciceId).orElseThrow().getStatut())
                .isEqualTo(StatutExercice.EN_ATTENTE_RELECTURE);
        assertThat(relectures.findAll()).singleElement()
                .extracting(r -> r.getRelecteur().getId()).isEqualTo(boris);
    }
}
