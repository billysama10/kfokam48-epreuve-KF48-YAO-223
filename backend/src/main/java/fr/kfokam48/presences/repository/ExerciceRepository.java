package fr.kfokam48.presences.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import fr.kfokam48.presences.domain.Exercice;

public interface ExerciceRepository extends JpaRepository<Exercice, Long> {

    boolean existsBySessionIdAndEtudiantId(Long sessionId, Long etudiantId);
}
