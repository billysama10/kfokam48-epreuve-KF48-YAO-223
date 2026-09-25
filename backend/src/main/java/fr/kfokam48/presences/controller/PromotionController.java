package fr.kfokam48.presences.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import fr.kfokam48.presences.dto.EtudiantDto;
import fr.kfokam48.presences.dto.PromotionDto;
import fr.kfokam48.presences.dto.SessionResumeDto;
import fr.kfokam48.presences.service.PromotionService;
import fr.kfokam48.presences.service.SessionService;

@RestController
@RequestMapping("/api/promotions")
public class PromotionController {

    private final PromotionService service;
    private final SessionService sessions;

    public PromotionController(PromotionService service, SessionService sessions) {
        this.service = service;
        this.sessions = sessions;
    }

    @GetMapping
    public List<PromotionDto> lister() {
        return service.lister();
    }

    @GetMapping("/{id}/etudiants")
    public List<EtudiantDto> etudiants(@PathVariable Long id) {
        return service.etudiants(id);
    }

    @GetMapping("/{id}/sessions")
    public List<SessionResumeDto> sessions(@PathVariable Long id) {
        return sessions.listerParPromotion(id);
    }
}
