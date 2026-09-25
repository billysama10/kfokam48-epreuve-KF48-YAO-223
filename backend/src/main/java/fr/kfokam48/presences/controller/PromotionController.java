package fr.kfokam48.presences.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import fr.kfokam48.presences.dto.EtudiantDto;
import fr.kfokam48.presences.dto.PromotionDto;
import fr.kfokam48.presences.service.PromotionService;

@RestController
@RequestMapping("/api/promotions")
public class PromotionController {

    private final PromotionService service;

    public PromotionController(PromotionService service) {
        this.service = service;
    }

    @GetMapping
    public List<PromotionDto> lister() {
        return service.lister();
    }

    @GetMapping("/{id}/etudiants")
    public List<EtudiantDto> etudiants(@PathVariable Long id) {
        return service.etudiants(id);
    }
}
