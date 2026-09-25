package fr.kfokam48.presences.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import fr.kfokam48.presences.dto.DeposerExerciceRequete;
import fr.kfokam48.presences.dto.ExerciceDto;
import fr.kfokam48.presences.service.ExerciceService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/exercices")
public class ExerciceController {

    private final ExerciceService service;

    public ExerciceController(ExerciceService service) {
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ExerciceDto deposer(@Valid @RequestBody DeposerExerciceRequete requete) {
        return service.deposer(requete);
    }
}
