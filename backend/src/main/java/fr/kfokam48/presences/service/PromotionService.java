package fr.kfokam48.presences.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import fr.kfokam48.presences.domain.Promotion;
import fr.kfokam48.presences.dto.EtudiantDto;
import fr.kfokam48.presences.dto.PromotionDto;
import fr.kfokam48.presences.erreur.ApiException;
import fr.kfokam48.presences.repository.EtudiantRepository;
import fr.kfokam48.presences.repository.PromotionRepository;

@Service
@Transactional(readOnly = true)
public class PromotionService {

    private final PromotionRepository promotions;
    private final EtudiantRepository etudiants;

    public PromotionService(PromotionRepository promotions, EtudiantRepository etudiants) {
        this.promotions = promotions;
        this.etudiants = etudiants;
    }

    public List<PromotionDto> lister() {
        return promotions.findAll().stream().map(PromotionDto::de).toList();
    }

    /** EF1, Q1 : la liste dans laquelle l'étudiant choisit son nom. */
    public List<EtudiantDto> etudiants(Long promotionId) {
        exiger(promotionId);
        return etudiants.findByPromotionIdOrderByNom(promotionId).stream().map(EtudiantDto::de).toList();
    }

    public Promotion exiger(Long promotionId) {
        return promotions.findById(promotionId)
                .orElseThrow(() -> ApiException.introuvable("PROMOTION_INCONNUE", "Cette promotion n'existe pas."));
    }
}
