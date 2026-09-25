package fr.kfokam48.presences.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/** Corps de POST /api/presences (opération imposée). */
public record MarquerPresenceRequete(
        @NotBlank String code,
        @NotNull Long etudiantId) {
}
