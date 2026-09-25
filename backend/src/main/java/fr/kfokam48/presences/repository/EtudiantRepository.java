package fr.kfokam48.presences.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import fr.kfokam48.presences.domain.Etudiant;

public interface EtudiantRepository extends JpaRepository<Etudiant, Long> {

    List<Etudiant> findByPromotionIdOrderByNom(Long promotionId);
}
