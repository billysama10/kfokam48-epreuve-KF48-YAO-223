package fr.kfokam48.presences.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import fr.kfokam48.presences.domain.Etudiant;
import fr.kfokam48.presences.domain.Exercice;
import fr.kfokam48.presences.domain.Promotion;
import fr.kfokam48.presences.domain.Relecture;
import fr.kfokam48.presences.domain.SessionCours;
import fr.kfokam48.presences.repository.EtudiantRepository;
import fr.kfokam48.presences.repository.ExerciceRepository;
import fr.kfokam48.presences.repository.PromotionRepository;
import fr.kfokam48.presences.repository.RelectureRepository;
import fr.kfokam48.presences.repository.SessionCoursRepository;

/** EF6 : GET /api/etudiants/{id}/relectures. */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class EtudiantControllerTest {

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

    @Test
    void leRelecteurVoitLExerciceQuiLuiEstAttribue() throws Exception {
        Promotion promotion = promotions.save(new Promotion("Promo"));
        Etudiant awa = etudiants.save(new Etudiant("Awa", promotion));
        Etudiant boris = etudiants.save(new Etudiant("Boris", promotion));
        LocalDateTime maintenant = LocalDateTime.now(ZoneOffset.UTC);
        SessionCours session = sessions.save(new SessionCours("Cours Spring", promotion, "REL001", maintenant));
        Exercice exercice = exercices.save(new Exercice(session, awa, "https://github.com/awa/tp", maintenant));
        Long relectureId = relectures.save(new Relecture(exercice, boris, maintenant)).getId();

        mockMvc.perform(get("/api/etudiants/" + boris.getId() + "/relectures"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(relectureId))
                .andExpect(jsonPath("$[0].sessionTitre").value("Cours Spring"))
                .andExpect(jsonPath("$[0].lien").value("https://github.com/awa/tp"))
                .andExpect(jsonPath("$[0].rendue").value(false));

        mockMvc.perform(get("/api/etudiants/" + awa.getId() + "/relectures"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void unEtudiantInconnuRenvoie404() throws Exception {
        mockMvc.perform(get("/api/etudiants/999/relectures"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("ETUDIANT_INCONNU"));
    }
}
