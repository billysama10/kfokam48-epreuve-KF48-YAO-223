package fr.kfokam48.presences.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import fr.kfokam48.presences.domain.SessionCours;

public interface SessionCoursRepository extends JpaRepository<SessionCours, Long> {

    Optional<SessionCours> findByCode(String code);

    boolean existsByCode(String code);
}
