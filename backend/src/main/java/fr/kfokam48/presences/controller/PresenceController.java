package fr.kfokam48.presences.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import fr.kfokam48.presences.dto.MarquerPresenceRequete;
import fr.kfokam48.presences.dto.PresenceDto;
import fr.kfokam48.presences.service.PresenceService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/presences")
public class PresenceController {

    private final PresenceService service;

    public PresenceController(PresenceService service) {
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PresenceDto marquer(@Valid @RequestBody MarquerPresenceRequete requete) {
        return service.marquer(requete);
    }
}
