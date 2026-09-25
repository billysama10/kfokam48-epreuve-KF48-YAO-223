package fr.kfokam48.presences.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import fr.kfokam48.presences.domain.Relecture;

public interface RelectureRepository extends JpaRepository<Relecture, Long> {

    List<Relecture> findByExerciceId(Long exerciceId);

    long countByExerciceIdAndRendueAtIsNotNull(Long exerciceId);

    List<Relecture> findByRelecteurIdOrderByAttribueeAtDesc(Long relecteurId);

    /**
     * Tableau (EF8, EF15, RG18, RG23) : chaque note rendue avec son exercice, son auteur et le statut de l'exercice,
     * pour calculer la note de chaque exercice (moyenne de ses relectures) et son caractère provisoire.
     */
    @Query("select x.id, x.etudiant.id, x.statut, r.note from Relecture r join r.exercice x "
            + "where x.etudiant.promotion.id = :promotionId and r.rendueAt is not null")
    List<Object[]> notesRendues(@Param("promotionId") Long promotionId);

    /** Tableau (EF8, Q16) : relectures attribuées à chaque étudiant et pas encore rendues. */
    @Query("select r.relecteur.id, count(r) from Relecture r "
            + "where r.relecteur.promotion.id = :promotionId and r.rendueAt is null group by r.relecteur.id")
    List<Object[]> enAttenteParRelecteur(@Param("promotionId") Long promotionId);

    /** Charge d'un relecteur dans une session, pour équilibrer le tirage (RG9). */
    long countByRelecteurIdAndExerciceSessionId(Long relecteurId, Long sessionId);
}
