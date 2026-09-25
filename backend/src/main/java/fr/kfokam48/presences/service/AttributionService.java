package fr.kfokam48.presences.service;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
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

/** EF5 : attribution automatique de deux relecteurs à chaque exercice déposé (changement de besoin, #28). */
@Service
public class AttributionService {

    /** RG8 : deux relecteurs différents par exercice (remplace « un seul relecteur », Q6). */
    public static final int RELECTEURS_PAR_EXERCICE = 2;

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
     * Complète l'exercice jusqu'à deux relecteurs (RG8), tirés au hasard parmi les présents de la session
     * (RG9), jamais l'auteur (RG2) ni un relecteur déjà attribué (RG22), les moins chargés d'abord.
     * S'il manque des candidats, on attribue ce qui est possible ; le reste le sera à la prochaine présence (RG10).
     */
    @Transactional
    public List<Relecture> attribuer(Exercice exercice) {
        if (exercice.getStatut() == StatutExercice.RELU) {
            return List.of();
        }
        List<Relecture> existantes = relectures.findByExerciceId(exercice.getId());
        int manquants = RELECTEURS_PAR_EXERCICE - existantes.size();
        if (manquants <= 0) {
            return List.of();
        }

        Set<Long> exclus = new HashSet<>();
        exclus.add(exercice.getEtudiant().getId());
        existantes.forEach(r -> exclus.add(r.getRelecteur().getId()));
        Long sessionId = exercice.getSession().getId();
        List<Etudiant> candidats = new ArrayList<>(presences.findBySessionId(sessionId).stream()
                .map(Presence::getEtudiant)
                .filter(e -> !exclus.contains(e.getId()))
                .toList());

        List<Relecture> nouvelles = new ArrayList<>();
        while (nouvelles.size() < manquants && !candidats.isEmpty()) {
            Etudiant relecteur = tirerLeMoinsCharge(candidats, sessionId).orElseThrow();
            candidats.remove(relecteur);
            nouvelles.add(relectures.save(new Relecture(exercice, relecteur, LocalDateTime.now(horloge))));
        }
        if (!nouvelles.isEmpty() && exercice.getStatut() == StatutExercice.DEPOSE) {
            exercice.changerStatut(StatutExercice.EN_ATTENTE_RELECTURE);
        }
        return nouvelles;
    }

    /** RG10 : à chaque nouvelle présence, les exercices qui n'ont pas encore leurs deux relecteurs sont complétés. */
    @Transactional
    public void attribuerEnAttente(Long sessionId) {
        exercices.findBySessionIdAndStatutIn(sessionId,
                List.of(StatutExercice.DEPOSE, StatutExercice.EN_ATTENTE_RELECTURE)).forEach(this::attribuer);
    }

    /** RG9 : au hasard parmi les candidats qui ont le moins de relectures dans la session. */
    private Optional<Etudiant> tirerLeMoinsCharge(List<Etudiant> candidats, Long sessionId) {
        Map<Long, List<Etudiant>> parCharge = candidats.stream().collect(Collectors.groupingBy(
                e -> relectures.countByRelecteurIdAndExerciceSessionId(e.getId(), sessionId)));
        return parCharge.entrySet().stream()
                .min(Comparator.comparing(Map.Entry::getKey))
                .map(Map.Entry::getValue)
                .map(moinsCharges -> moinsCharges.get(hasard.nextInt(moinsCharges.size())));
    }
}
