package fr.kfokam48.presences.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/** Corps de POST /api/exercices (opération imposée). */
public record DeposerExerciceRequete(
        @NotNull Long sessionId,
        @NotNull Long etudiantId,
        @NotBlank String lien) {
}
