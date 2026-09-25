package fr.kfokam48.presences.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import fr.kfokam48.presences.dto.RelectureAttribueeDto;
import fr.kfokam48.presences.service.RelectureService;

@RestController
@RequestMapping("/api/etudiants")
public class EtudiantController {

    private final RelectureService relectures;

    public EtudiantController(RelectureService relectures) {
        this.relectures = relectures;
    }

    @GetMapping("/{id}/relectures")
    public List<RelectureAttribueeDto> relectures(@PathVariable Long id) {
        return relectures.attribuees(id);
    }
}
