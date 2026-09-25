package fr.kfokam48.presences.dto;

import fr.kfokam48.presences.domain.Etudiant;

public record EtudiantDto(Long id, String nom, Long promotionId) {

    public static EtudiantDto de(Etudiant e) {
        return new EtudiantDto(e.getId(), e.getNom(), e.getPromotion().getId());
    }
}
