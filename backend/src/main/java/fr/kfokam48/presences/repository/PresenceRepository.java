package fr.kfokam48.presences.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import fr.kfokam48.presences.domain.Presence;

public interface PresenceRepository extends JpaRepository<Presence, Long> {

    boolean existsBySessionIdAndEtudiantId(Long sessionId, Long etudiantId);

    List<Presence> findBySessionId(Long sessionId);

    /** Tableau (EF8) : nombre de sessions où chaque étudiant de la promotion est présent. */
    @Query("select p.etudiant.id, count(p) from Presence p where p.etudiant.promotion.id = :promotionId group by p.etudiant.id")
    List<Object[]> compterParEtudiant(@Param("promotionId") Long promotionId);
}
