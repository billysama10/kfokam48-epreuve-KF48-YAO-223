package fr.kfokam48.presences.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
        Map<Long, Double> moyennes = relectures.moyenneParAuteur(promotionId).stream()
                .collect(Collectors.toMap(l -> (Long) l[0], l -> ((Number) l[1]).doubleValue()));

        return etudiants.findByPromotionIdOrderByNom(promotionId).stream()
                .map(e -> new LigneTableauDto(e.getId(), e.getNom(),
                        nbPresences.getOrDefault(e.getId(), 0L),
                        nbExercices.getOrDefault(e.getId(), 0L),
                        arrondi(moyennes.get(e.getId())),
                        nbEnAttente.getOrDefault(e.getId(), 0L)))
                .toList();
    }

    /** RG18 : moyenne arrondie à 2 décimales par l'API, null sans note ; le frontend ne recalcule rien. */
    static BigDecimal arrondi(Double moyenne) {
        return moyenne == null ? null : BigDecimal.valueOf(moyenne).setScale(2, RoundingMode.HALF_UP);
    }

    private static Map<Long, Long> compte(List<Object[]> lignes) {
        return lignes.stream().collect(Collectors.toMap(l -> (Long) l[0], l -> ((Number) l[1]).longValue()));
    }
}
