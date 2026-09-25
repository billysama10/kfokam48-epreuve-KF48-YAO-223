package fr.kfokam48.presences.dto;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;

import fr.kfokam48.presences.domain.SessionCours;

/** Schéma SessionResume du contrat ; le statut est déduit, jamais stocké (D2). */
public record SessionResumeDto(Long id, String titre, Long promotionId, String code,
        OffsetDateTime ouvertureAt, OffsetDateTime expirationAt, OffsetDateTime clotureeAt, String statut) {

    public static SessionResumeDto de(SessionCours s, LocalDateTime maintenant) {
        String statut = s.estCloturee() ? "CLOTUREE" : s.codeExpire(maintenant) ? "EXPIREE" : "OUVERTE";
        return new SessionResumeDto(s.getId(), s.getTitre(), s.getPromotion().getId(), s.getCode(),
                Dates.utc(s.getOuvertureAt()), Dates.utc(s.getExpirationAt()), Dates.utc(s.getClotureeAt()), statut);
    }
}
