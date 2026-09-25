package fr.kfokam48.presences.service;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Objects;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import fr.kfokam48.presences.domain.Etudiant;
import fr.kfokam48.presences.domain.Exercice;
import fr.kfokam48.presences.domain.SessionCours;
import fr.kfokam48.presences.dto.DeposerExerciceRequete;
import fr.kfokam48.presences.dto.ExerciceDto;
import fr.kfokam48.presences.erreur.ApiException;
import fr.kfokam48.presences.repository.EtudiantRepository;
import fr.kfokam48.presences.repository.ExerciceRepository;
import fr.kfokam48.presences.repository.SessionCoursRepository;

@Service
public class ExerciceService {

    private final SessionCoursRepository sessions;
    private final EtudiantRepository etudiants;
    private final ExerciceRepository exercices;
    private final AttributionService attribution;
    private final Clock horloge;

    public ExerciceService(SessionCoursRepository sessions, EtudiantRepository etudiants,
            ExerciceRepository exercices, AttributionService attribution, Clock horloge) {
        this.sessions = sessions;
        this.etudiants = etudiants;
        this.exercices = exercices;
        this.attribution = attribution;
        this.horloge = horloge;
    }

    /**
     * EF4 : dépôt du lien d'un exercice. Autorisé après la fin de session et même pour
     * un absent, tant que la session n'est pas clôturée (RG13, RG16).
     */
    @Transactional
    public ExerciceDto deposer(DeposerExerciceRequete requete) {
        if (!LienExercice.valide(requete.lien())) {
            throw ApiException.requeteInvalide("LIEN_INVALIDE", "Le lien doit être une adresse http ou https valide.");
        }
        // Opération imposée : seuls 400 et 409 sont prévus, un identifiant inconnu est donc un 400
        // Bug #26 : même verrou que les présences, l'attribution ne peut pas se croiser
        SessionCours session = sessions.verrouillerParId(requete.sessionId())
                .orElseThrow(() -> ApiException.requeteInvalide("SESSION_INCONNUE", "Cette session n'existe pas."));
        Etudiant etudiant = etudiants.findById(requete.etudiantId())
                .orElseThrow(() -> ApiException.requeteInvalide("ETUDIANT_INCONNU", "Cet étudiant n'existe pas."));
        // RG20
        if (!Objects.equals(etudiant.getPromotion().getId(), session.getPromotion().getId())) {
            throw ApiException.requeteInvalide("ETUDIANT_HORS_PROMOTION",
                    "Cet étudiant n'appartient pas à la promotion de cette session.");
        }
        // RG16
        if (session.estCloturee()) {
            throw ApiException.conflit("SESSION_CLOTUREE", "Cette session est clôturée.");
        }
        // RG12 : un exercice par étudiant et par session
        if (exercices.existsBySessionIdAndEtudiantId(session.getId(), etudiant.getId())) {
            throw ApiException.conflit("EXERCICE_DEJA_DEPOSE", "Un exercice est déjà déposé pour cette session.");
        }

        Exercice exercice = new Exercice(session, etudiant, requete.lien().trim(), LocalDateTime.now(horloge));
        exercice = exercices.save(exercice);
        // EF5 : relecteur tiré dès le dépôt s'il existe un autre présent (RG9, RG10)
        attribution.attribuer(exercice);
        return ExerciceDto.de(exercice);
    }
}
