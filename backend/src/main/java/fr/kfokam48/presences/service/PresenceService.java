package fr.kfokam48.presences.service;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Locale;
import java.util.Objects;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import fr.kfokam48.presences.domain.Etudiant;
import fr.kfokam48.presences.domain.Presence;
import fr.kfokam48.presences.domain.SessionCours;
import fr.kfokam48.presences.domain.SourcePresence;
import fr.kfokam48.presences.dto.MarquerPresenceRequete;
import fr.kfokam48.presences.dto.PresenceDto;
import fr.kfokam48.presences.erreur.ApiException;
import fr.kfokam48.presences.repository.EtudiantRepository;
import fr.kfokam48.presences.repository.PresenceRepository;
import fr.kfokam48.presences.repository.SessionCoursRepository;

@Service
public class PresenceService {

    private final SessionCoursRepository sessions;
    private final EtudiantRepository etudiants;
    private final PresenceRepository presences;
    private final AttributionService attribution;
    private final Clock horloge;

    public PresenceService(SessionCoursRepository sessions, EtudiantRepository etudiants,
            PresenceRepository presences, AttributionService attribution, Clock horloge) {
        this.sessions = sessions;
        this.etudiants = etudiants;
        this.presences = presences;
        this.attribution = attribution;
        this.horloge = horloge;
    }

    /**
     * EF3 : marquer sa présence avec le code. Ordre des contrôles du diagramme D3 :
     * code inconnu (400), code expiré ou session clôturée (410), déjà présent (409).
     */
    @Transactional
    public PresenceDto marquer(MarquerPresenceRequete requete) {
        String code = requete.code().trim().toUpperCase(Locale.ROOT);
        // Bug #26 : la session est verrouillée jusqu'à la fin de la transaction
        SessionCours session = sessions.verrouillerParCode(code)
                .orElseThrow(() -> ApiException.requeteInvalide("CODE_INCONNU", "Code de présence inconnu."));

        LocalDateTime maintenant = LocalDateTime.now(horloge);
        // RG1, RG4 : plus de présence par code après l'expiration ; RG16 : ni après la clôture
        if (session.codeExpire(maintenant) || session.estCloturee()) {
            throw new ApiException(HttpStatus.GONE, "CODE_EXPIRE", "Le code de présence a expiré.");
        }

        Etudiant etudiant = etudiants.findById(requete.etudiantId())
                .orElseThrow(() -> ApiException.requeteInvalide("ETUDIANT_INCONNU", "Cet étudiant n'existe pas."));
        // RG20 : seul un étudiant de la promotion de la session peut marquer sa présence
        if (!Objects.equals(etudiant.getPromotion().getId(), session.getPromotion().getId())) {
            throw ApiException.requeteInvalide("ETUDIANT_HORS_PROMOTION",
                    "Cet étudiant n'appartient pas à la promotion de cette session.");
        }

        // RG5 : une seule présence par étudiant et par session
        if (presences.existsBySessionIdAndEtudiantId(session.getId(), etudiant.getId())) {
            throw ApiException.conflit("DEJA_PRESENT", "Présence déjà enregistrée pour cette session.");
        }

        // RG6 : une présence marquée avec le code a la source ETUDIANT
        Presence presence = presences.save(new Presence(session, etudiant, SourcePresence.ETUDIANT, maintenant));
        // RG10 : un nouveau présent peut relire les exercices restés sans relecteur
        attribution.attribuerEnAttente(session.getId());
        return PresenceDto.de(presence);
    }
}
