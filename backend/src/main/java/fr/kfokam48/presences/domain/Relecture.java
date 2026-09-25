package fr.kfokam48.presences.domain;

import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

/** Le relecteur est un étudiant dans un rôle (cahier des charges, section 2). */
@Entity
public class Relecture {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Deux relectures par exercice depuis V3 (RG8, #28). */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "exercice_id")
    private Exercice exercice;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "relecteur_id")
    private Etudiant relecteur;

    private Integer note;
    private String commentaire;
    private LocalDateTime attribueeAt;
    private LocalDateTime rendueAt;

    protected Relecture() {
    }

    public Relecture(Exercice exercice, Etudiant relecteur, LocalDateTime attribueeAt) {
        this.exercice = exercice;
        this.relecteur = relecteur;
        this.attribueeAt = attribueeAt;
    }

    public boolean estRendue() {
        return rendueAt != null;
    }

    /** EF7 : la relecture rendue est définitive (RG11) ; le passage à RELU dépend de la seconde (RelectureService). */
    public void rendre(int note, String commentaire, LocalDateTime maintenant) {
        this.note = note;
        this.commentaire = commentaire;
        this.rendueAt = maintenant;
    }

    public Long getId() { return id; }
    public Exercice getExercice() { return exercice; }
    public Etudiant getRelecteur() { return relecteur; }
    public Integer getNote() { return note; }
    public String getCommentaire() { return commentaire; }
    public LocalDateTime getAttribueeAt() { return attribueeAt; }
    public LocalDateTime getRendueAt() { return rendueAt; }
}
