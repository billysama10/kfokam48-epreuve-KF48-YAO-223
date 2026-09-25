package fr.kfokam48.presences.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import fr.kfokam48.presences.dto.LigneTableauDto;
import fr.kfokam48.presences.service.TableauService;

@RestController
@RequestMapping("/api/tableau")
public class TableauController {

    private final TableauService service;

    public TableauController(TableauService service) {
        this.service = service;
    }

    @GetMapping
    public List<LigneTableauDto> tableau(@RequestParam Long promotionId) {
        return service.tableau(promotionId);
    }
}
