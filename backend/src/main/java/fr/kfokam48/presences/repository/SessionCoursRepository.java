package fr.kfokam48.presences.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import fr.kfokam48.presences.domain.SessionCours;
import jakarta.persistence.LockModeType;

public interface SessionCoursRepository extends JpaRepository<SessionCours, Long> {

    Optional<SessionCours> findByCode(String code);

    boolean existsByCode(String code);

    List<SessionCours> findByPromotionIdOrderByOuvertureAtDesc(Long promotionId);

    /**
     * Bug #26 : verrou d'écriture sur la session (SELECT … FOR UPDATE). Les présences et les dépôts
     * d'une même session sont ainsi traités l'un après l'autre : la vérification « déjà présent »
     * et l'attribution des relecteurs lisent toujours l'état validé par la requête précédente.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select s from SessionCours s where s.code = :code")
    Optional<SessionCours> verrouillerParCode(@Param("code") String code);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select s from SessionCours s where s.id = :id")
    Optional<SessionCours> verrouillerParId(@Param("id") Long id);
}
