package fr.kfokam48.presences.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;

import fr.kfokam48.presences.domain.Etudiant;
import fr.kfokam48.presences.domain.Presence;
import fr.kfokam48.presences.domain.Promotion;
import fr.kfokam48.presences.domain.SessionCours;
import fr.kfokam48.presences.domain.SourcePresence;
import fr.kfokam48.presences.dto.MarquerPresenceRequete;
import fr.kfokam48.presences.dto.PresenceDto;
import fr.kfokam48.presences.erreur.ApiException;
import fr.kfokam48.presences.repository.EtudiantRepository;
import fr.kfokam48.presences.repository.PresenceRepository;
import fr.kfokam48.presences.repository.SessionCoursRepository;

/**
 * Test unitaire de la règle RG1 (et RG4) : le code de présence expire
 * 15 minutes après l'ouverture de la session, et à l'instant exact
 * de l'expiration il ne fonctionne déjà plus. L'heure est figée par une Clock.
 */
class PresenceServiceTest {

    private static final LocalDateTime OUVERTURE = LocalDateTime.of(2026, 9, 25, 9, 0, 0);

    private final SessionCoursRepository sessions = mock(SessionCoursRepository.class);
    private final EtudiantRepository etudiants = mock(EtudiantRepository.class);
    private final PresenceRepository presences = mock(PresenceRepository.class);

    private SessionCours session;
    private Etudiant etudiant;

    @BeforeEach
    void donnees() {
        Promotion promotion = new Promotion("Promo");
        ReflectionTestUtils.setField(promotion, "id", 1L);
        session = new SessionCours("Cours", promotion, "ABC234", OUVERTURE);
        ReflectionTestUtils.setField(session, "id", 10L);
        etudiant = new Etudiant("Awa", promotion);
        ReflectionTestUtils.setField(etudiant, "id", 100L);

        when(sessions.findByCode("ABC234")).thenReturn(Optional.of(session));
        when(etudiants.findById(100L)).thenReturn(Optional.of(etudiant));
        when(presences.save(any(Presence.class))).thenAnswer(i -> i.getArgument(0));
    }

    private PresenceService serviceA(LocalDateTime maintenant) {
        Clock horloge = Clock.fixed(maintenant.toInstant(ZoneOffset.UTC), ZoneOffset.UTC);
        return new PresenceService(sessions, etudiants, presences, horloge);
    }

    @Test
    void uneSecondeAvantLExpirationLaPresenceEstEnregistree() {
        PresenceDto dto = serviceA(OUVERTURE.plusMinutes(15).minusSeconds(1))
                .marquer(new MarquerPresenceRequete("ABC234", 100L));

        assertThat(dto.source()).isEqualTo(SourcePresence.ETUDIANT);
        assertThat(dto.etudiantId()).isEqualTo(100L);
    }

    @Test
    void aLInstantExactDeLExpirationLeCodeEstRefuseEn410() {
        PresenceService service = serviceA(OUVERTURE.plusMinutes(15));

        assertThatThrownBy(() -> service.marquer(new MarquerPresenceRequete("ABC234", 100L)))
                .isInstanceOfSatisfying(ApiException.class, e -> {
                    assertThat(e.getStatut()).isEqualTo(HttpStatus.GONE);
                    assertThat(e.getCode()).isEqualTo("CODE_EXPIRE");
                });
        verify(presences, never()).save(any());
    }

    @Test
    void leCodeEstReconnuQuelleQueSoitLaCasse() {
        PresenceDto dto = serviceA(OUVERTURE.plusMinutes(1))
                .marquer(new MarquerPresenceRequete(" abc234 ", 100L));

        assertThat(dto.source()).isEqualTo(SourcePresence.ETUDIANT);
    }
}
