package fr.kfokam48.presences.dto;

import fr.kfokam48.presences.domain.Exercice;
import fr.kfokam48.presences.domain.Relecture;

/**
 * Schéma RelectureAttribuee du contrat : ce que le relecteur doit relire.
 * L'id est celui à utiliser dans POST /api/relectures/{id}.
 */
public record RelectureAttribueeDto(Long id, Long exerciceId, Long sessionId, String sessionTitre,
        String lien, boolean rendue, Integer note, String commentaire) {

    public static RelectureAttribueeDto de(Relecture r) {
        Exercice exercice = r.getExercice();
        return new RelectureAttribueeDto(r.getId(), exercice.getId(), exercice.getSession().getId(),
                exercice.getSession().getTitre(), exercice.getLien(), r.estRendue(), r.getNote(), r.getCommentaire());
    }
}
