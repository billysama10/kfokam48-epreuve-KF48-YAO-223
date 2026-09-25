package fr.kfokam48.presences.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/** Corps de POST /api/sessions (opération imposée). */
public record OuvrirSessionRequete(
        @NotBlank @Size(max = 150) String titre,
        @NotNull Long promotionId) {
}
