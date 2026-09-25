package fr.kfokam48.presences.service;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.JsonNode;

import fr.kfokam48.presences.domain.Relecture;
import fr.kfokam48.presences.domain.StatutExercice;
import fr.kfokam48.presences.dto.RelectureAttribueeDto;
import fr.kfokam48.presences.dto.RendreRelectureRequete;
import fr.kfokam48.presences.erreur.ApiException;
import fr.kfokam48.presences.repository.EtudiantRepository;
import fr.kfokam48.presences.repository.RelectureRepository;

@Service
public class RelectureService {

    static final int NOTE_MIN = 0;
    static final int NOTE_MAX = 20;

    private final RelectureRepository relectures;
    private final EtudiantRepository etudiants;
    private final Clock horloge;

    public RelectureService(RelectureRepository relectures, EtudiantRepository etudiants, Clock horloge) {
        this.relectures = relectures;
        this.etudiants = etudiants;
        this.horloge = horloge;
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

    /**
     * EF7 : le relecteur attribué rend une note entière de 0 à 20 et un commentaire.
     * L'appelant est identifié par l'en-tête X-Etudiant-Id (cahier des charges, section 7).
     */
    @Transactional
    public RelectureAttribueeDto rendre(Long relectureId, Long appelantId, RendreRelectureRequete requete) {
        Relecture relecture = relectures.findById(relectureId)
                .orElseThrow(() -> ApiException.introuvable("RELECTURE_INCONNUE", "Cette relecture n'existe pas."));
        int note = noteValide(requete.note());

        // RG2 : jamais sa propre copie ; RG19 : seul le relecteur attribué rend la relecture
        if (Objects.equals(appelantId, relecture.getExercice().getEtudiant().getId())) {
            throw new ApiException(HttpStatus.FORBIDDEN, "AUTO_RELECTURE", "Vous ne pouvez pas relire votre propre exercice.");
        }
        if (!Objects.equals(appelantId, relecture.getRelecteur().getId())) {
            throw new ApiException(HttpStatus.FORBIDDEN, "RELECTEUR_NON_ASSIGNE", "Cette relecture ne vous est pas attribuée.");
        }
        // RG11 (Q15) : une relecture rendue est définitive
        if (relecture.estRendue()) {
            throw ApiException.conflit("RELECTURE_DEJA_RENDUE", "Cette relecture a déjà été rendue.");
        }
        // RG16
        if (relecture.getExercice().getSession().estCloturee()) {
            throw ApiException.conflit("SESSION_CLOTUREE", "Cette session est clôturée.");
        }

        relecture.rendre(note, requete.commentaire().trim(), LocalDateTime.now(horloge));
        // D4 v2 : l'exercice n'est RELU qu'une fois ses deux relectures rendues (RG8, #28)
        if (relectures.countByExerciceIdAndRendueAtIsNotNull(relecture.getExercice().getId())
                >= AttributionService.RELECTEURS_PAR_EXERCICE) {
            relecture.getExercice().changerStatut(StatutExercice.RELU);
        }
        return RelectureAttribueeDto.de(relecture);
    }

    /** RG3 : la note est un entier de 0 à 20 ; 12.5, "12" ou 21 sont refusés. */
    static int noteValide(JsonNode note) {
        boolean entiere = note != null && note.isIntegralNumber() && note.canConvertToInt();
        if (!entiere || note.intValue() < NOTE_MIN || note.intValue() > NOTE_MAX) {
            throw ApiException.requeteInvalide("NOTE_INVALIDE", "La note doit être un entier compris entre 0 et 20.");
        }
        return note.intValue();
    }
}
