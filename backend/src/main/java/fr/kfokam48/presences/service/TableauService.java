package fr.kfokam48.presences.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import fr.kfokam48.presences.domain.StatutExercice;
import fr.kfokam48.presences.dto.LigneTableauDto;
import fr.kfokam48.presences.erreur.ApiException;
import fr.kfokam48.presences.repository.EtudiantRepository;
import fr.kfokam48.presences.repository.ExerciceRepository;
import fr.kfokam48.presences.repository.PresenceRepository;
import fr.kfokam48.presences.repository.PromotionRepository;
import fr.kfokam48.presences.repository.RelectureRepository;

/**
 * EF8 : tableau du formateur. Quatre requêtes groupées par promotion, quel que soit
 * le nombre d'étudiants (ENF2 : moins de 2 s pour 60 étudiants).
 */
@Service
public class TableauService {

    private final PromotionRepository promotions;
    private final EtudiantRepository etudiants;
    private final PresenceRepository presences;
    private final ExerciceRepository exercices;
    private final RelectureRepository relectures;

    public TableauService(PromotionRepository promotions, EtudiantRepository etudiants,
            PresenceRepository presences, ExerciceRepository exercices, RelectureRepository relectures) {
        this.promotions = promotions;
        this.etudiants = etudiants;
        this.presences = presences;
        this.exercices = exercices;
        this.relectures = relectures;
    }

    @Transactional(readOnly = true)
    public List<LigneTableauDto> tableau(Long promotionId) {
        if (!promotions.existsById(promotionId)) {
            throw ApiException.introuvable("PROMOTION_INCONNUE", "Cette promotion n'existe pas.");
        }
        Map<Long, Long> nbPresences = compte(presences.compterParEtudiant(promotionId));
        Map<Long, Long> nbExercices = compte(exercices.compterParEtudiant(promotionId));
        Map<Long, Long> nbEnAttente = compte(relectures.enAttenteParRelecteur(promotionId));
        Map<Long, List<NoteExercice>> notesParAuteur = notesParAuteur(relectures.notesRendues(promotionId));

        return etudiants.findByPromotionIdOrderByNom(promotionId).stream()
                .map(e -> {
                    List<NoteExercice> notes = notesParAuteur.getOrDefault(e.getId(), List.of());
                    return new LigneTableauDto(e.getId(), e.getNom(),
                            nbPresences.getOrDefault(e.getId(), 0L),
                            nbExercices.getOrDefault(e.getId(), 0L),
                            arrondi(notes.stream().mapToDouble(NoteExercice::note).average()),
                            notes.stream().anyMatch(NoteExercice::provisoire),
                            nbEnAttente.getOrDefault(e.getId(), 0L));
                })
                .toList();
    }

    /** Note d'un exercice (RG23) : moyenne de ses relectures rendues, provisoire tant qu'il n'est pas RELU. */
    record NoteExercice(double note, boolean provisoire) {
    }

    /**
     * Regroupe les notes rendues par exercice, puis par auteur. Un exercice RELU avec une seule relecture
     * (rendue avant le changement de besoin) garde une note définitive : pas d'effet rétroactif (RG23).
     */
    static Map<Long, List<NoteExercice>> notesParAuteur(List<Object[]> lignes) {
        Map<Long, List<Integer>> notesParExercice = new HashMap<>();
        Map<Long, Long> auteurParExercice = new HashMap<>();
        Map<Long, StatutExercice> statutParExercice = new HashMap<>();
        for (Object[] l : lignes) {
            Long exerciceId = (Long) l[0];
            auteurParExercice.put(exerciceId, (Long) l[1]);
            statutParExercice.put(exerciceId, (StatutExercice) l[2]);
            notesParExercice.computeIfAbsent(exerciceId, k -> new ArrayList<>()).add(((Number) l[3]).intValue());
        }
        return notesParExercice.entrySet().stream().collect(Collectors.groupingBy(
                entree -> auteurParExercice.get(entree.getKey()),
                Collectors.mapping(entree -> new NoteExercice(
                        entree.getValue().stream().mapToInt(Integer::intValue).average().orElseThrow(),
                        statutParExercice.get(entree.getKey()) != StatutExercice.RELU),
                        Collectors.toList())));
    }

    /** RG18 : moyenne arrondie à 2 décimales par l'API, null sans note ; le frontend ne recalcule rien. */
    static BigDecimal arrondi(java.util.OptionalDouble moyenne) {
        return moyenne.isEmpty() ? null : BigDecimal.valueOf(moyenne.getAsDouble()).setScale(2, RoundingMode.HALF_UP);
    }

    private static Map<Long, Long> compte(List<Object[]> lignes) {
        return lignes.stream().collect(Collectors.toMap(l -> (Long) l[0], l -> ((Number) l[1]).longValue()));
    }
}
