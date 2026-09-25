package fr.kfokam48.presences.dto;

import fr.kfokam48.presences.domain.Exercice;
import fr.kfokam48.presences.domain.StatutExercice;

/** Réponse 201 de POST /api/exercices : { id, statut }. */
public record ExerciceDto(Long id, StatutExercice statut) {

    public static ExerciceDto de(Exercice e) {
        return new ExerciceDto(e.getId(), e.getStatut());
    }
}
