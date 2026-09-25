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
import fr.kfokam48.presences.domain.Presence;
import fr.kfokam48.presences.domain.Promotion;
import fr.kfokam48.presences.domain.Relecture;
import fr.kfokam48.presences.domain.SessionCours;
import fr.kfokam48.presences.domain.SourcePresence;
import fr.kfokam48.presences.repository.EtudiantRepository;
import fr.kfokam48.presences.repository.ExerciceRepository;
import fr.kfokam48.presences.repository.PresenceRepository;
import fr.kfokam48.presences.repository.PromotionRepository;
import fr.kfokam48.presences.repository.RelectureRepository;
import fr.kfokam48.presences.repository.SessionCoursRepository;

/** EF8 : GET /api/tableau, opération imposée par le contrat. */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class TableauControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PromotionRepository promotions;

    @Autowired
    private EtudiantRepository etudiants;

    @Autowired
    private SessionCoursRepository sessions;

    @Autowired
    private PresenceRepository presences;

    @Autowired
    private ExerciceRepository exercices;

    @Autowired
    private RelectureRepository relectures;

    @Test
    void uneLigneParEtudiantAvecUneMoyenneArrondieParLApi() throws Exception {
        LocalDateTime t = LocalDateTime.now(ZoneOffset.UTC);
        Promotion promotion = promotions.save(new Promotion("Promo"));
        Etudiant awa = etudiants.save(new Etudiant("Awa", promotion));
        Etudiant boris = etudiants.save(new Etudiant("Boris", promotion));
        Etudiant carine = etudiants.save(new Etudiant("Carine", promotion));

        // Awa reçoit 10, 11 et 11 sur trois sessions : moyenne 10,666… arrondie à 10.67
        int[] notes = {10, 11, 11};
        for (int i = 0; i < notes.length; i++) {
            SessionCours s = sessions.save(new SessionCours("Cours " + i, promotion, "TAB00" + i, t));
            presences.save(new Presence(s, awa, SourcePresence.ETUDIANT, t));
            Exercice x = exercices.save(new Exercice(s, awa, "https://github.com/awa/" + i, t));
            Relecture r = relectures.save(new Relecture(x, boris, t));
            r.rendre(notes[i], "ok", t);
        }
        // Boris doit encore relire l'exercice de Carine (relecture non rendue)
        SessionCours s = sessions.save(new SessionCours("Cours 3", promotion, "TAB003", t));
        Exercice exerciceCarine = exercices.save(new Exercice(s, carine, "https://github.com/carine/3", t));
        relectures.save(new Relecture(exerciceCarine, boris, t));

        mockMvc.perform(get("/api/tableau").param("promotionId", promotion.getId().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(3))
                .andExpect(jsonPath("$[0].nom").value("Awa"))
                .andExpect(jsonPath("$[0].presences").value(3))
                .andExpect(jsonPath("$[0].exercicesDeposes").value(3))
                .andExpect(jsonPath("$[0].moyenne").value(10.67))
                .andExpect(jsonPath("$[0].relecturesEnAttente").value(0))
                .andExpect(jsonPath("$[1].nom").value("Boris"))
                .andExpect(jsonPath("$[1]").value(org.hamcrest.Matchers.hasEntry("moyenne", null)))
                .andExpect(jsonPath("$[1].relecturesEnAttente").value(1))
                .andExpect(jsonPath("$[2].nom").value("Carine"))
                .andExpect(jsonPath("$[2].exercicesDeposes").value(1))
                .andExpect(jsonPath("$[2]").value(org.hamcrest.Matchers.hasEntry("moyenne", null)));
    }

    @Test
    void unePromotionInconnueRenvoie404() throws Exception {
        mockMvc.perform(get("/api/tableau").param("promotionId", "999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("PROMOTION_INCONNUE"));
    }

    @Test
    void sansPromotionIdLaRequeteEstInvalide() throws Exception {
        mockMvc.perform(get("/api/tableau"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("REQUETE_INVALIDE"));
    }
}
