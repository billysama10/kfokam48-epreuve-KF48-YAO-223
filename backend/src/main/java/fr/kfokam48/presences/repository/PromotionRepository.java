package fr.kfokam48.presences.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import fr.kfokam48.presences.domain.Promotion;

public interface PromotionRepository extends JpaRepository<Promotion, Long> {
}
