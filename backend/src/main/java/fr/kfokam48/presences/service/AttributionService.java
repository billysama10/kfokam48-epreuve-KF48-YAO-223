package fr.kfokam48.presences.service;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.random.RandomGenerator;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import fr.kfokam48.presences.domain.Etudiant;
import fr.kfokam48.presences.domain.Exercice;
import fr.kfokam48.presences.domain.Presence;
import fr.kfokam48.presences.domain.Relecture;
import fr.kfokam48.presences.domain.StatutExercice;
import fr.kfokam48.presences.repository.ExerciceRepository;
import fr.kfokam48.presences.repository.PresenceRepository;
import fr.kfokam48.presences.repository.RelectureRepository;

/** EF5 : attribution automatique d'un relecteur à chaque exercice déposé. */
@Service
public class AttributionService {

    private final PresenceRepository presences;
    private final ExerciceRepository exercices;
    private final RelectureRepository relectures;
    private final Clock horloge;
    private final RandomGenerator hasard;

    public AttributionService(PresenceRepository presences, ExerciceRepository exercices,
            RelectureRepository relectures, Clock horloge, RandomGenerator hasard) {
        this.presences = presences;
        this.exercices = exercices;
        this.relectures = relectures;
        this.horloge = horloge;
        this.hasard = hasard;
    }

    /**
     * Tire un relecteur au hasard parmi les présents de la session (RG9), jamais l'auteur (RG2),
     * en privilégiant les moins chargés. Sans candidat, l'exercice reste DEPOSE (RG10).
     * Un exercice qui a déjà un relecteur n'en reçoit pas un second (RG8).
     */
    @Transactional
    public Optional<Relecture> attribuer(Exercice exercice) {
        if (exercice.getStatut() != StatutExercice.DEPOSE || relectures.existsByExerciceId(exercice.getId())) {
            return Optional.empty();
        }
        Long sessionId = exercice.getSession().getId();
        List<Etudiant> candidats = presences.findBySessionId(sessionId).stream()
                .map(Presence::getEtudiant)
                .filter(e -> !Objects.equals(e.getId(), exercice.getEtudiant().getId()))
                .toList();
        if (candidats.isEmpty()) {
            return Optional.empty();
        }

        Map<Long, List<Etudiant>> parCharge = candidats.stream().collect(Collectors.groupingBy(
                e -> relectures.countByRelecteurIdAndExerciceSessionId(e.getId(), sessionId)));
        List<Etudiant> moinsCharges = parCharge.entrySet().stream()
                .min(Comparator.comparing(Map.Entry::getKey))
                .orElseThrow()
                .getValue();
        Etudiant relecteur = moinsCharges.get(hasard.nextInt(moinsCharges.size()));

        Relecture relecture = relectures.save(new Relecture(exercice, relecteur, LocalDateTime.now(horloge)));
        exercice.changerStatut(StatutExercice.EN_ATTENTE_RELECTURE);
        return Optional.of(relecture);
    }

    /** RG10 : à chaque nouvelle présence, les exercices restés sans relecteur sont retentés. */
    @Transactional
    public void attribuerEnAttente(Long sessionId) {
        exercices.findBySessionIdAndStatut(sessionId, StatutExercice.DEPOSE).forEach(this::attribuer);
    }
}
