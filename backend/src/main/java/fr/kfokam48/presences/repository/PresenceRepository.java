package fr.kfokam48.presences.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import fr.kfokam48.presences.domain.Presence;

public interface PresenceRepository extends JpaRepository<Presence, Long> {

    boolean existsBySessionIdAndEtudiantId(Long sessionId, Long etudiantId);
}
