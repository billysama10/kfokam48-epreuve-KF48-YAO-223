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
import fr.kfokam48.presences.domain.Exercice;
import fr.kfokam48.presences.domain.Promotion;
import fr.kfokam48.presences.domain.Relecture;
import fr.kfokam48.presences.domain.SessionCours;
import fr.kfokam48.presences.domain.StatutExercice;
import fr.kfokam48.presences.repository.EtudiantRepository;
import fr.kfokam48.presences.repository.ExerciceRepository;
import fr.kfokam48.presences.repository.PromotionRepository;
import fr.kfokam48.presences.repository.RelectureRepository;
import fr.kfokam48.presences.repository.SessionCoursRepository;

/** EF7 : POST /api/relectures/{id}, opération imposée par le contrat. */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class RelectureControllerTest {

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

    private Long relectureId;
    private Long exerciceId;
    private Long auteur;
    private Long relecteur;
    private Long autre;

    @BeforeEach
    void donnees() {
        Promotion promotion = promotions.save(new Promotion("Promo"));
        Etudiant awa = etudiants.save(new Etudiant("Awa", promotion));
        Etudiant boris = etudiants.save(new Etudiant("Boris", promotion));
        auteur = awa.getId();
        relecteur = boris.getId();
        autre = etudiants.save(new Etudiant("Carine", promotion)).getId();
        LocalDateTime maintenant = LocalDateTime.now(ZoneOffset.UTC);
        SessionCours session = sessions.save(new SessionCours("Cours", promotion, "NOTE01", maintenant));
        Exercice exercice = new Exercice(session, awa, "https://github.com/awa/tp", maintenant);
        exercice.changerStatut(StatutExercice.EN_ATTENTE_RELECTURE);
        exerciceId = exercices.save(exercice).getId();
        relectureId = relectures.save(new Relecture(exercice, boris, maintenant)).getId();
    }

    private ResultActions rendre(Long appelant, String note) throws Exception {
        return mockMvc.perform(post("/api/relectures/" + relectureId)
                .header("X-Etudiant-Id", appelant)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"note\":" + note + ",\"commentaire\":\"Bon travail\"}"));
    }

    @Test
    void leRelecteurRendSaNote200EtLExercicePasseARelu() throws Exception {
        rendre(relecteur, "15")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rendue").value(true))
                .andExpect(jsonPath("$.note").value(15));

        assertThat(exercices.findById(exerciceId).orElseThrow().getStatut()).isEqualTo(StatutExercice.RELU);
    }

    @Test
    void uneNoteHorsBornesOuNonEntiereRenvoie400() throws Exception {
        for (String note : new String[] {"21", "-1", "12.5", "\"12\"", "null"}) {
            rendre(relecteur, note)
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value("NOTE_INVALIDE"));
        }
    }

    @Test
    void lesBornes0Et20SontAcceptees() throws Exception {
        rendre(relecteur, "20").andExpect(status().isOk());
    }

    @Test
    void lAuteurNePeutPasSeRelire403() throws Exception {
        rendre(auteur, "20")
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("AUTO_RELECTURE"));
    }

    @Test
    void unEtudiantNonAttribueRecoit403() throws Exception {
        rendre(autre, "10")
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("RELECTEUR_NON_ASSIGNE"));
    }

    @Test
    void uneRelectureDejaRendueNePeutPlusChanger409() throws Exception {
        rendre(relecteur, "15").andExpect(status().isOk());

        rendre(relecteur, "18")
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("RELECTURE_DEJA_RENDUE"));
    }

    @Test
    void uneRelectureInconnueRenvoie404() throws Exception {
        mockMvc.perform(post("/api/relectures/999999").header("X-Etudiant-Id", relecteur)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"note\":10,\"commentaire\":\"ok\"}"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("RELECTURE_INCONNUE"));
    }
}
