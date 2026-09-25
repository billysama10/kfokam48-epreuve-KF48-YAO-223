package fr.kfokam48.presences.dto;

import java.math.BigDecimal;

/** Une ligne de GET /api/tableau (opération imposée) ; moyenne null sans note reçue. */
public record LigneTableauDto(Long etudiantId, String nom, long presences, long exercicesDeposes,
        BigDecimal moyenne, long relecturesEnAttente) {
}
