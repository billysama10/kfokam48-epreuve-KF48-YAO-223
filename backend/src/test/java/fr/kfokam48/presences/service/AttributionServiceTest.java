package fr.kfokam48.presences.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
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

/**
 * Tests unitaires de l'attribution, version deux relecteurs (#28) :
 * RG2 (jamais l'auteur), RG8 (deux relecteurs), RG9 (moins chargés), RG10 (complément), RG22 (distincts).
 */
class AttributionServiceTest {

    private static final LocalDateTime T0 = LocalDateTime.of(2026, 9, 25, 9, 0);

    private final PresenceRepository presences = mock(PresenceRepository.class);
    private final ExerciceRepository exercices = mock(ExerciceRepository.class);
    private final RelectureRepository relectures = mock(RelectureRepository.class);

    private SessionCours session;
    private Etudiant auteur;
    private Etudiant boris;
    private Etudiant carine;
    private Etudiant david;

    @BeforeEach
    void donnees() {
        Promotion promotion = new Promotion("Promo");
        session = new SessionCours("Cours", promotion, "ABC234", T0);
        ReflectionTestUtils.setField(session, "id", 10L);
        auteur = etudiant(1L, "Awa", promotion);
        boris = etudiant(2L, "Boris", promotion);
        carine = etudiant(3L, "Carine", promotion);
        david = etudiant(4L, "David", promotion);
        when(relectures.save(any(Relecture.class))).thenAnswer(i -> i.getArgument(0));
        when(relectures.findByExerciceId(anyLong())).thenReturn(List.of());
    }

    private static Etudiant etudiant(Long id, String nom, Promotion promotion) {
        Etudiant e = new Etudiant(nom, promotion);
        ReflectionTestUtils.setField(e, "id", id);
        return e;
    }

    private void presents(Etudiant... etudiants) {
        when(presences.findBySessionId(10L)).thenReturn(
                Arrays.stream(etudiants).map(e -> new Presence(session, e, SourcePresence.ETUDIANT, T0)).toList());
    }

    private Exercice exerciceDeAwa() {
        Exercice exercice = new Exercice(session, auteur, "https://github.com/awa/tp", T0);
        ReflectionTestUtils.setField(exercice, "id", 100L);
        return exercice;
    }

    private AttributionService service(long graine) {
        return new AttributionService(presences, exercices, relectures, Clock.systemUTC(), new Random(graine));
    }

    private static List<Etudiant> relecteurs(List<Relecture> relectures) {
        return relectures.stream().map(Relecture::getRelecteur).toList();
    }

    @Test
    void deuxRelecteursDifferentsJamaisLAuteurSurCentTirages() {
        presents(auteur, boris, carine);
        for (long graine = 0; graine < 100; graine++) {
            Exercice exercice = exerciceDeAwa();
            List<Relecture> attribuees = service(graine).attribuer(exercice);

            assertThat(relecteurs(attribuees)).containsExactlyInAnyOrder(boris, carine).doesNotContain(auteur);
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
    void avecUnSeulAutrePresentUnRelecteurPuisLeSecondALaPresenceSuivante() {
        presents(auteur, boris);
        Exercice exercice = exerciceDeAwa();
        List<Relecture> premiere = service(1).attribuer(exercice);
        assertThat(relecteurs(premiere)).containsExactly(boris);
        assertThat(exercice.getStatut()).isEqualTo(StatutExercice.EN_ATTENTE_RELECTURE);

        // Carine arrive : Boris est déjà relecteur, seule Carine peut compléter (RG10, RG22)
        presents(auteur, boris, carine);
        when(relectures.findByExerciceId(100L)).thenReturn(premiere);
        assertThat(relecteurs(service(2).attribuer(exercice))).containsExactly(carine);
    }

    @Test
    void lesPresentsLesMoinsChargesSontPrivilegies() {
        presents(auteur, boris, carine, david);
        when(relectures.countByRelecteurIdAndExerciceSessionId(eq(2L), anyLong())).thenReturn(3L);

        for (long graine = 0; graine < 20; graine++) {
            assertThat(relecteurs(service(graine).attribuer(exerciceDeAwa()))).containsExactlyInAnyOrder(carine, david);
        }
    }

    @Test
    void unExerciceQuiADejaSesDeuxRelecteursNEnRecoitPasDeTroisieme() {
        presents(auteur, boris, carine, david);
        Exercice exercice = exerciceDeAwa();
        when(relectures.findByExerciceId(100L)).thenReturn(
                List.of(new Relecture(exercice, boris, T0), new Relecture(exercice, carine, T0)));

        assertThat(service(1).attribuer(exercice)).isEmpty();
        verify(relectures, never()).save(any());
    }

    @Test
    void uneNouvellePresenceCompleteLesExercicesEnAttente() {
        presents(auteur, boris, carine);
        Exercice exercice = exerciceDeAwa();
        when(exercices.findBySessionIdAndStatutIn(eq(10L), anyList())).thenReturn(List.of(exercice));

        service(1).attribuerEnAttente(10L);

        assertThat(exercice.getStatut()).isEqualTo(StatutExercice.EN_ATTENTE_RELECTURE);
        verify(relectures, org.mockito.Mockito.times(2)).save(any(Relecture.class));
    }
}
