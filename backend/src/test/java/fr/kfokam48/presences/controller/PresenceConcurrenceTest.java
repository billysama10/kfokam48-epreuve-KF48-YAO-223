package fr.kfokam48.presences.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;

import fr.kfokam48.presences.domain.Etudiant;
import fr.kfokam48.presences.domain.Promotion;
import fr.kfokam48.presences.domain.SessionCours;
import fr.kfokam48.presences.repository.EtudiantRepository;
import fr.kfokam48.presences.repository.PromotionRepository;
import fr.kfokam48.presences.repository.SessionCoursRepository;

/**
 * Bug #26 : deux étudiants qui saisissent le code en même temps doivent être enregistrés tous les deux.
 * Pas de rollback de test ici : les requêtes simultanées doivent vraiment valider leurs transactions.
 * La base est vidée après chaque test pour ne pas gêner les autres.
 */
@SpringBootTest
@AutoConfigureMockMvc
class PresenceConcurrenceTest {

    private static final int TOURS = 5;
    private static final int ETUDIANTS_SIMULTANES = 4;
    private static final int EXERCICES_EN_ATTENTE = 3;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PromotionRepository promotions;

    @Autowired
    private EtudiantRepository etudiants;

    @Autowired
    private SessionCoursRepository sessions;

    @Autowired
    private JdbcTemplate jdbc;

    @AfterEach
    void viderLaBase() {
        for (String table : new String[] {"relecture", "exercice", "presence", "session_cours", "etudiant", "promotion"}) {
            jdbc.update("delete from " + table);
        }
    }

    private int presence(String code, Long etudiantId) throws Exception {
        return mockMvc.perform(post("/api/presences").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"code\":\"" + code + "\",\"etudiantId\":" + etudiantId + "}"))
                .andReturn().getResponse().getStatus();
    }

    private int depot(Long sessionId, Long etudiantId) throws Exception {
        return mockMvc.perform(post("/api/exercices").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"sessionId\":" + sessionId + ",\"etudiantId\":" + etudiantId
                                + ",\"lien\":\"https://github.com/tp/" + etudiantId + "\"}"))
                .andReturn().getResponse().getStatus();
    }

    /** Lance les appels au même instant (barrière) et renvoie les statuts HTTP obtenus. */
    private List<Integer> simultanement(List<java.util.concurrent.Callable<Integer>> appels) throws Exception {
        ExecutorService pool = Executors.newFixedThreadPool(appels.size());
        CyclicBarrier depart = new CyclicBarrier(appels.size());
        try {
            List<Future<Integer>> resultats = new ArrayList<>();
            for (var appel : appels) {
                resultats.add(pool.submit(() -> {
                    depart.await();
                    return appel.call();
                }));
            }
            List<Integer> statuts = new ArrayList<>();
            for (Future<Integer> r : resultats) {
                statuts.add(r.get());
            }
            return statuts;
        } finally {
            pool.shutdownNow();
        }
    }

    @Test
    void desPresencesSimultaneesSontToutesEnregistrees() throws Exception {
        for (int tour = 0; tour < TOURS; tour++) {
            Promotion promotion = promotions.save(new Promotion("Promo " + tour));
            SessionCours session = sessions.save(new SessionCours("Cours " + tour, promotion, "CONC0" + tour,
                    LocalDateTime.now(ZoneOffset.UTC)));

            // Des exercices attendent un relecteur : leurs auteurs ont déposé sans être présents (RG13)
            for (int i = 0; i < EXERCICES_EN_ATTENTE; i++) {
                Long auteur = etudiants.save(new Etudiant("Auteur " + i, promotion)).getId();
                assertThat(depot(session.getId(), auteur)).isEqualTo(201);
            }

            List<java.util.concurrent.Callable<Integer>> appels = new ArrayList<>();
            for (int i = 0; i < ETUDIANTS_SIMULTANES; i++) {
                Long etudiantId = etudiants.save(new Etudiant("Présent " + i, promotion)).getId();
                appels.add(() -> presence(session.getCode(), etudiantId));
            }

            List<Integer> statuts = simultanement(appels);

            assertThat(statuts).as("statuts HTTP au tour %d", tour).containsOnly(201);
            assertThat(jdbc.queryForObject("select count(*) from presence where session_id = ?", Long.class,
                    session.getId())).as("présences enregistrées au tour %d", tour).isEqualTo(ETUDIANTS_SIMULTANES);
            assertThat(jdbc.queryForObject("select count(*) from relecture r join exercice x on r.exercice_id = x.id "
                    + "where x.session_id = ?", Long.class, session.getId()))
                    .as("un relecteur par exercice au tour %d", tour).isEqualTo(EXERCICES_EN_ATTENTE);
        }
    }

    @Test
    void unDoubleEnvoiSimultaneDonne201Puis409JamaisUn500() throws Exception {
        for (int tour = 0; tour < TOURS; tour++) {
            Promotion promotion = promotions.save(new Promotion("Promo double " + tour));
            SessionCours session = sessions.save(new SessionCours("Cours", promotion, "DBL00" + tour,
                    LocalDateTime.now(ZoneOffset.UTC)));
            Long etudiantId = etudiants.save(new Etudiant("Awa", promotion)).getId();

            List<Integer> statuts = simultanement(List.of(
                    () -> presence(session.getCode(), etudiantId),
                    () -> presence(session.getCode(), etudiantId)));

            assertThat(statuts).as("statuts HTTP au tour %d", tour).containsExactlyInAnyOrder(201, 409);
        }
    }
}
