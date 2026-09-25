package fr.kfokam48.presences.dto;

import fr.kfokam48.presences.domain.Presence;
import fr.kfokam48.presences.domain.SourcePresence;

/** Réponse 201 de POST /api/presences : { id, sessionId, etudiantId, source }. */
public record PresenceDto(Long id, Long sessionId, Long etudiantId, SourcePresence source) {

    public static PresenceDto de(Presence p) {
        return new PresenceDto(p.getId(), p.getSession().getId(), p.getEtudiant().getId(), p.getSource());
    }
}
