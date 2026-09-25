package fr.kfokam48.presences.controller;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import fr.kfokam48.presences.dto.RelectureAttribueeDto;
import fr.kfokam48.presences.dto.RendreRelectureRequete;
import fr.kfokam48.presences.service.RelectureService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/relectures")
public class RelectureController {

    private final RelectureService service;

    public RelectureController(RelectureService service) {
        this.service = service;
    }

    /** Opération imposée : 200 si la relecture est enregistrée. */
    @PostMapping("/{id}")
    public RelectureAttribueeDto rendre(@PathVariable Long id,
            @RequestHeader("X-Etudiant-Id") Long appelantId,
            @Valid @RequestBody RendreRelectureRequete requete) {
        return service.rendre(id, appelantId, requete);
    }
}
