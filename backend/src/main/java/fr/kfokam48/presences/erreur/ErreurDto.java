package fr.kfokam48.presences.erreur;

/** Format d'erreur imposé par le contrat, pour toutes les erreurs sans exception. */
public record ErreurDto(String code, String message) {
}
