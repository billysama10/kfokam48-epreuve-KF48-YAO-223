package fr.kfokam48.presences.dto;

import java.math.BigDecimal;

/**
 * Une ligne de GET /api/tableau (opération imposée) ; moyenne null sans note reçue.
 * moyenneProvisoire : ajout de l'étape 3 (#29), vrai si une note d'exercice est provisoire.
 */
public record LigneTableauDto(Long etudiantId, String nom, long presences, long exercicesDeposes,
        BigDecimal moyenne, boolean moyenneProvisoire, long relecturesEnAttente) {
}
