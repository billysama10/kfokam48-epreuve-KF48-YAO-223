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
public class Presence {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "session_id")
    private SessionCours session;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "etudiant_id")
    private Etudiant etudiant;

    @Enumerated(EnumType.STRING)
    private SourcePresence source;

    private LocalDateTime marqueeAt;

    protected Presence() {
    }

    public Presence(SessionCours session, Etudiant etudiant, SourcePresence source, LocalDateTime marqueeAt) {
        this.session = session;
        this.etudiant = etudiant;
        this.source = source;
        this.marqueeAt = marqueeAt;
    }

    public Long getId() { return id; }
    public SessionCours getSession() { return session; }
    public Etudiant getEtudiant() { return etudiant; }
    public SourcePresence getSource() { return source; }
    public LocalDateTime getMarqueeAt() { return marqueeAt; }
}
