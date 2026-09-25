package fr.kfokam48.presences.dto;

import java.time.OffsetDateTime;

import fr.kfokam48.presences.domain.SessionCours;

/** Réponse 201 de POST /api/sessions : { id, code, ouvertureAt, expirationAt }. */
public record SessionOuverteDto(Long id, String code, OffsetDateTime ouvertureAt, OffsetDateTime expirationAt) {

    public static SessionOuverteDto de(SessionCours s) {
        return new SessionOuverteDto(s.getId(), s.getCode(), Dates.utc(s.getOuvertureAt()), Dates.utc(s.getExpirationAt()));
    }
}
