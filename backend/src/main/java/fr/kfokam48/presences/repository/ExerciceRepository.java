package fr.kfokam48.presences.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import fr.kfokam48.presences.domain.Exercice;
import fr.kfokam48.presences.domain.StatutExercice;

public interface ExerciceRepository extends JpaRepository<Exercice, Long> {

    boolean existsBySessionIdAndEtudiantId(Long sessionId, Long etudiantId);

    List<Exercice> findBySessionIdAndStatut(Long sessionId, StatutExercice statut);

    /** Tableau (EF8) : nombre d'exercices déposés par chaque étudiant de la promotion. */
    @Query("select x.etudiant.id, count(x) from Exercice x where x.etudiant.promotion.id = :promotionId group by x.etudiant.id")
    List<Object[]> compterParEtudiant(@Param("promotionId") Long promotionId);
}
