# D3 — Séquence : marquer sa présence

Opération imposée **POST /api/presences** (EF3). Les codes HTTP et les codes d'erreur sont **exactement** ceux de [api/contrat.yaml](../../api/contrat.yaml) : **201**, **400 CODE_INCONNU**, **409 DEJA_PRESENT**, **410 CODE_EXPIRE**. Toute erreur passe par le **@RestControllerAdvice** et renvoie **{ "code": "...", "message": "..." }** (ENF4).

Règles citées : RG1 et RG4 (expiration du code, fin de session), RG5 (une seule présence), RG6 (source **ETUDIANT**), RG9 et RG10 (retentative d'attribution d'un relecteur), RG16 (session clôturée).

```mermaid
sequenceDiagram
    autonumber
    actor E as Étudiant
    participant F as Front React (src/api/presences.ts)
    participant C as PresenceController
    participant S as PresenceService
    participant SR as SessionRepository
    participant PR as PresenceRepository
    participant A as AttributionService
    participant H as ErreurHandler (@RestControllerAdvice)

    E->>F: saisit le code
    F->>C: POST /api/presences { code, etudiantId }
    C->>S: marquer(code, etudiantId)
    S->>SR: findByCode(code)

    alt code inconnu
        SR-->>S: aucune session
        S-->>H: CodeInconnuException
        H-->>F: 400 { "code": "CODE_INCONNU", "message": "Code de présence inconnu." }
        F-->>E: affiche « Code inconnu »
    else code expiré ou session clôturée (RG1, RG4, RG16)
        SR-->>S: session, maintenant ≥ expirationAt ou clotureeAt renseigné
        S-->>H: CodeExpireException
        H-->>F: 410 { "code": "CODE_EXPIRE", "message": "Le code de présence a expiré." }
        F-->>E: affiche « Code expiré, adressez-vous au formateur »
    else déjà présent (RG5)
        SR-->>S: session valide
        S->>PR: existsBySessionIdAndEtudiantId(sessionId, etudiantId)
        PR-->>S: vrai
        S-->>H: DejaPresentException
        H-->>F: 409 { "code": "DEJA_PRESENT", "message": "Présence déjà enregistrée pour cette session." }
        F-->>E: affiche « Vous êtes déjà présent »
    else cas nominal
        SR-->>S: session valide
        S->>PR: existsBySessionIdAndEtudiantId(sessionId, etudiantId)
        PR-->>S: faux
        S->>PR: save(presence, source = ETUDIANT) (RG6)
        PR-->>S: presence
        S->>A: attribuerExercicesEnAttente(sessionId) (RG9, RG10)
        A-->>S: exercices DEPOSE éventuellement attribués
        S-->>C: PresenceDto
        C-->>F: 201 { id, sessionId, etudiantId, source: "ETUDIANT" }
        F-->>E: affiche « Présence enregistrée »
    end
```

## Correspondance avec le contrat

| Branche | Statut HTTP | Code d'erreur | Règle |
|---|---|---|---|
| Code inconnu | 400 | **CODE_INCONNU** | — |
| Code expiré ou session clôturée | 410 | **CODE_EXPIRE** | RG1, RG4, RG16 |
| Déjà présent | 409 | **DEJA_PRESENT** | RG5 |
| Cas nominal | 201 | — (corps **{ id, sessionId, etudiantId, source }**) | RG6, RG10 |

**Ordre des contrôles** : le code est d'abord cherché, puis sa validité, puis l'unicité de la présence. Un étudiant déjà présent qui ressaisit un code expiré reçoit donc **410**.

**Autres 400 possibles, hors du schéma pour le garder lisible** : champ manquant (**REQUETE_INVALIDE**, rejeté par la validation avant le service), étudiant inconnu (**ETUDIANT_INCONNU**), étudiant d'une autre promotion (**ETUDIANT_HORS_PROMOTION**, RG20). Le blocage après cinq erreurs (EF14, RG21) est Could et n'apparaît pas ici.
