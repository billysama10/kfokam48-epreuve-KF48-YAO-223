package fr.kfokam48.presences.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import fr.kfokam48.presences.domain.Exercice;
import fr.kfokam48.presences.domain.StatutExercice;

public interface ExerciceRepository extends JpaRepository<Exercice, Long> {

    boolean existsBySessionIdAndEtudiantId(Long sessionId, Long etudiantId);

    List<Exercice> findBySessionIdAndStatut(Long sessionId, StatutExercice statut);
}
