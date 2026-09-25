package fr.kfokam48.presences.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import fr.kfokam48.presences.domain.Relecture;

public interface RelectureRepository extends JpaRepository<Relecture, Long> {

    boolean existsByExerciceId(Long exerciceId);

    List<Relecture> findByRelecteurIdOrderByAttribueeAtDesc(Long relecteurId);

    /** Charge d'un relecteur dans une session, pour équilibrer le tirage (RG9). */
    long countByRelecteurIdAndExerciceSessionId(Long relecteurId, Long sessionId);
}
