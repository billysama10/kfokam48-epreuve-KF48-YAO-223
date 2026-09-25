package fr.kfokam48.presences.service;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import fr.kfokam48.presences.domain.Promotion;
import fr.kfokam48.presences.domain.SessionCours;
import fr.kfokam48.presences.dto.OuvrirSessionRequete;
import fr.kfokam48.presences.dto.SessionOuverteDto;
import fr.kfokam48.presences.erreur.ApiException;
import fr.kfokam48.presences.repository.PromotionRepository;
import fr.kfokam48.presences.repository.SessionCoursRepository;

@Service
public class SessionService {

    private static final int ESSAIS_CODE = 20;

    private final SessionCoursRepository sessions;
    private final PromotionRepository promotions;
    private final GenerateurCode generateur;
    private final Clock horloge;

    public SessionService(SessionCoursRepository sessions, PromotionRepository promotions,
            GenerateurCode generateur, Clock horloge) {
        this.sessions = sessions;
        this.promotions = promotions;
        this.generateur = generateur;
        this.horloge = horloge;
    }

    /** EF2 : ouvre une session dont le code expire 15 minutes plus tard (RG1). */
    @Transactional
    public SessionOuverteDto ouvrir(OuvrirSessionRequete requete) {
        // Opération imposée : seul 400 est prévu, une promotion inconnue est donc un 400.
        Promotion promotion = promotions.findById(requete.promotionId())
                .orElseThrow(() -> ApiException.requeteInvalide("PROMOTION_INCONNUE", "Cette promotion n'existe pas."));
        LocalDateTime maintenant = LocalDateTime.now(horloge).truncatedTo(ChronoUnit.SECONDS);
        SessionCours session = new SessionCours(requete.titre().trim(), promotion, codeLibre(), maintenant);
        return SessionOuverteDto.de(sessions.save(session));
    }

    /** Le code est unique pour toutes les sessions (contrainte UNIQUE de V1). */
    private String codeLibre() {
        for (int i = 0; i < ESSAIS_CODE; i++) {
            String code = generateur.generer();
            if (!sessions.existsByCode(code)) {
                return code;
            }
        }
        throw new IllegalStateException("Impossible de générer un code de présence libre");
    }
}
