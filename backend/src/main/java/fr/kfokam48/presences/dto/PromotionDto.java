package fr.kfokam48.presences.dto;

import fr.kfokam48.presences.domain.Promotion;

public record PromotionDto(Long id, String nom) {

    public static PromotionDto de(Promotion p) {
        return new PromotionDto(p.getId(), p.getNom());
    }
}
