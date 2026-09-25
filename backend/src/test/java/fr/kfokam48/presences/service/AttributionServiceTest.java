package fr.kfokam48.presences.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Random;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import fr.kfokam48.presences.domain.Etudiant;
import fr.kfokam48.presences.domain.Exercice;
import fr.kfokam48.presences.domain.Presence;
import fr.kfokam48.presences.domain.Promotion;
import fr.kfokam48.presences.domain.Relecture;
import fr.kfokam48.presences.domain.SessionCours;
import fr.kfokam48.presences.domain.SourcePresence;
import fr.kfokam48.presences.domain.StatutExercice;
import fr.kfokam48.presences.repository.ExerciceRepository;
import fr.kfokam48.presences.repository.PresenceRepository;
import fr.kfokam48.presences.repository.RelectureRepository;

/** Tests unitaires de RG2 (jamais l'auteur), RG9 (moins chargé) et RG10 (pas de candidat). */
class AttributionServiceTest {

    private static final LocalDateTime T0 = LocalDateTime.of(2026, 9, 25, 9, 0);

    private final PresenceRepository presences = mock(PresenceRepository.class);
    private final ExerciceRepository exercices = mock(ExerciceRepository.class);
    private final RelectureRepository relectures = mock(RelectureRepository.class);

    private SessionCours session;
    private Etudiant auteur;
    private Etudiant boris;
    private Etudiant carine;

    @BeforeEach
    void donnees() {
        Promotion promotion = new Promotion("Promo");
        session = new SessionCours("Cours", promotion, "ABC234", T0);
        ReflectionTestUtils.setField(session, "id", 10L);
        auteur = etudiant(1L, "Awa", promotion);
        boris = etudiant(2L, "Boris", promotion);
        carine = etudiant(3L, "Carine", promotion);
        when(relectures.save(any(Relecture.class))).thenAnswer(i -> i.getArgument(0));
    }

    private static Etudiant etudiant(Long id, String nom, Promotion promotion) {
        Etudiant e = new Etudiant(nom, promotion);
        ReflectionTestUtils.setField(e, "id", id);
        return e;
    }

    private void presents(Etudiant... etudiants) {
        when(presences.findBySessionId(10L)).thenReturn(
                java.util.Arrays.stream(etudiants).map(e -> new Presence(session, e, SourcePresence.ETUDIANT, T0)).toList());
    }

    private Exercice exerciceDeAwa() {
        Exercice exercice = new Exercice(session, auteur, "https://github.com/awa/tp", T0);
        ReflectionTestUtils.setField(exercice, "id", 100L);
        return exercice;
    }

    private AttributionService service(long graine) {
        return new AttributionService(presences, exercices, relectures, Clock.systemUTC(), new Random(graine));
    }

    @Test
    void lAuteurNEstJamaisTireMemeSurCentTirages() {
        presents(auteur, boris);
        for (long graine = 0; graine < 100; graine++) {
            Exercice exercice = exerciceDeAwa();
            Optional<Relecture> relecture = service(graine).attribuer(exercice);

            assertThat(relecture).isPresent();
            assertThat(relecture.get().getRelecteur()).isNotEqualTo(auteur).isEqualTo(boris);
            assertThat(exercice.getStatut()).isEqualTo(StatutExercice.EN_ATTENTE_RELECTURE);
        }
    }

    @Test
    void siLAuteurEstLeSeulPresentLExerciceResteDepose() {
        presents(auteur);
        Exercice exercice = exerciceDeAwa();

        assertThat(service(1).attribuer(exercice)).isEmpty();
        assertThat(exercice.getStatut()).isEqualTo(StatutExercice.DEPOSE);
        verify(relectures, never()).save(any());
    }

    @Test
    void lePresentLeMoinsChargeEstPrivilegie() {
        presents(auteur, boris, carine);
        when(relectures.countByRelecteurIdAndExerciceSessionId(eq(2L), anyLong())).thenReturn(2L);
        when(relectures.countByRelecteurIdAndExerciceSessionId(eq(3L), anyLong())).thenReturn(0L);

        for (long graine = 0; graine < 20; graine++) {
            assertThat(service(graine).attribuer(exerciceDeAwa()).orElseThrow().getRelecteur()).isEqualTo(carine);
        }
    }

    @Test
    void unExerciceQuiADejaUnRelecteurNEnRecoitPasUnSecond() {
        presents(auteur, boris);
        when(relectures.existsByExerciceId(100L)).thenReturn(true);

        assertThat(service(1).attribuer(exerciceDeAwa())).isEmpty();
        verify(relectures, never()).save(any());
    }

    @Test
    void uneNouvellePresenceDebloqueLesExercicesDeposes() {
        presents(auteur, boris);
        Exercice exercice = exerciceDeAwa();
        when(exercices.findBySessionIdAndStatut(10L, StatutExercice.DEPOSE)).thenReturn(List.of(exercice));

        service(1).attribuerEnAttente(10L);

        assertThat(exercice.getStatut()).isEqualTo(StatutExercice.EN_ATTENTE_RELECTURE);
    }
}
