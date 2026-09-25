package fr.kfokam48.presences.domain;

import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

@Entity
public class Exercice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "session_id")
    private SessionCours session;

    /** Auteur de l'exercice. */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "etudiant_id")
    private Etudiant etudiant;

    private String lien;

    @Enumerated(EnumType.STRING)
    private StatutExercice statut;

    private LocalDateTime deposeAt;
    private LocalDateTime modifieAt;

    protected Exercice() {
    }

    public Exercice(SessionCours session, Etudiant etudiant, String lien, LocalDateTime deposeAt) {
        this.session = session;
        this.etudiant = etudiant;
        this.lien = lien;
        this.deposeAt = deposeAt;
        this.statut = StatutExercice.DEPOSE;
    }

    public void changerStatut(StatutExercice statut) {
        this.statut = statut;
    }

    public Long getId() { return id; }
    public SessionCours getSession() { return session; }
    public Etudiant getEtudiant() { return etudiant; }
    public String getLien() { return lien; }
    public StatutExercice getStatut() { return statut; }
    public LocalDateTime getDeposeAt() { return deposeAt; }
    public LocalDateTime getModifieAt() { return modifieAt; }
}
