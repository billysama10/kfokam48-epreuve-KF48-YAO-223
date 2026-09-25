package fr.kfokam48.presences.dto;

import com.fasterxml.jackson.databind.JsonNode;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Corps de POST /api/relectures/{id} (opération imposée). La note est lue brute (JsonNode) :
 * Jackson convertirait sinon 12.5 en 12 sans erreur, alors que RG3 exige un 400 NOTE_INVALIDE.
 */
public record RendreRelectureRequete(
        JsonNode note,
        @NotBlank @Size(max = 2000) String commentaire) {
}
