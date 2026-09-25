package fr.kfokam48.presences.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import fr.kfokam48.presences.dto.RelectureAttribueeDto;
import fr.kfokam48.presences.erreur.ApiException;
import fr.kfokam48.presences.repository.EtudiantRepository;
import fr.kfokam48.presences.repository.RelectureRepository;

@Service
public class RelectureService {

    private final RelectureRepository relectures;
    private final EtudiantRepository etudiants;

    public RelectureService(RelectureRepository relectures, EtudiantRepository etudiants) {
        this.relectures = relectures;
        this.etudiants = etudiants;
    }

    /** EF6 : les relectures attribuées à un étudiant, les plus récentes en premier. */
    @Transactional(readOnly = true)
    public List<RelectureAttribueeDto> attribuees(Long relecteurId) {
        if (!etudiants.existsById(relecteurId)) {
            throw ApiException.introuvable("ETUDIANT_INCONNU", "Cet étudiant n'existe pas.");
        }
        return relectures.findByRelecteurIdOrderByAttribueeAtDesc(relecteurId).stream()
                .map(RelectureAttribueeDto::de)
                .toList();
    }
}
