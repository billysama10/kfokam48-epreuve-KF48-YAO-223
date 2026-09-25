package fr.kfokam48.presences.domain;

import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "session_cours")
public class SessionCours {

    /** RG1 : le code expire 15 minutes après l'ouverture. */
    public static final int DUREE_CODE_MINUTES = 15;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String titre;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "promotion_id")
    private Promotion promotion;

    private String code;
    private LocalDateTime ouvertureAt;
    private LocalDateTime expirationAt;
    private LocalDateTime clotureeAt;

    protected SessionCours() {
    }

    public SessionCours(String titre, Promotion promotion, String code, LocalDateTime ouvertureAt) {
        this.titre = titre;
        this.promotion = promotion;
        this.code = code;
        this.ouvertureAt = ouvertureAt;
        this.expirationAt = ouvertureAt.plusMinutes(DUREE_CODE_MINUTES);
    }

    /** RG1, RG4 : à expirationAt exactement, le code est déjà expiré. */
    public boolean codeExpire(LocalDateTime maintenant) {
        return !maintenant.isBefore(expirationAt);
    }

    /** RG16 : une session clôturée est figée. */
    public boolean estCloturee() {
        return clotureeAt != null;
    }

    public Long getId() { return id; }
    public String getTitre() { return titre; }
    public Promotion getPromotion() { return promotion; }
    public String getCode() { return code; }
    public LocalDateTime getOuvertureAt() { return ouvertureAt; }
    public LocalDateTime getExpirationAt() { return expirationAt; }
    public LocalDateTime getClotureeAt() { return clotureeAt; }
}
