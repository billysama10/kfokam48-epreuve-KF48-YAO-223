# D2 — Modèle de données

Ce diagramme décrit **exactement** les tables et colonnes de la future migration Flyway `V1__schema.sql`. Toute évolution du schéma passe par une nouvelle migration (`V2__…`, `V3__…`) **et** par la mise à jour de ce fichier dans le même commit.

Choix structurants (cahier des charges, sections 2 et 7) :
- le **relecteur n'a pas de table** : c'est un `etudiant` référencé par `relecture.relecteur_id` ;
- la **relecture est une entité séparée**, liée à un `exercice` (0 ou 1 relecture par exercice, RG8) et à l'étudiant relecteur ;
- une ligne `relecture` n'existe **que lorsqu'un relecteur est attribué** : un exercice sans relecture est au statut `DEPOSE` (RG10) ;
- la table s'appelle `session_cours` et non `session`, pour éviter un mot réservé SQL ;
- l'état d'une session (`OUVERTE`, `EXPIREE`, `CLOTUREE`) **n'est pas stocké** : il se déduit de `expiration_at` et `cloturee_at` ;
- la **moyenne n'est pas stockée** : l'API la calcule à partir de `relecture.note` (RG18).

```mermaid
erDiagram
    promotion ||--o{ etudiant : "regroupe"
    promotion ||--o{ session_cours : "a"
    session_cours ||--o{ presence : "enregistre"
    etudiant ||--o{ presence : "marque"
    session_cours ||--o{ exercice : "reçoit"
    etudiant ||--o{ exercice : "dépose (auteur)"
    exercice ||--o| relecture : "est relu par"
    etudiant ||--o{ relecture : "relit (relecteur)"

    promotion {
        bigint id PK "identity"
        varchar nom "NOT NULL, 100"
    }

    etudiant {
        bigint id PK "identity"
        varchar nom "NOT NULL, 100"
        bigint promotion_id FK "NOT NULL -> promotion.id"
    }

    session_cours {
        bigint id PK "identity"
        varchar titre "NOT NULL, 150"
        bigint promotion_id FK "NOT NULL -> promotion.id"
        varchar code UK "NOT NULL, 6, UNIQUE"
        timestamp ouverture_at "NOT NULL"
        timestamp expiration_at "NOT NULL = ouverture_at + 15 min (RG1)"
        timestamp cloturee_at "NULL tant que non clôturée (RG16)"
    }

    presence {
        bigint id PK "identity"
        bigint session_id FK "NOT NULL -> session_cours.id"
        bigint etudiant_id FK "NOT NULL -> etudiant.id"
        varchar source "NOT NULL, ETUDIANT ou FORMATEUR (RG6)"
        timestamp marquee_at "NOT NULL"
    }

    exercice {
        bigint id PK "identity"
        bigint session_id FK "NOT NULL -> session_cours.id"
        bigint etudiant_id FK "NOT NULL -> etudiant.id (auteur)"
        varchar lien "NOT NULL, 500 (RG14)"
        varchar statut "NOT NULL, DEPOSE, EN_ATTENTE_RELECTURE, RELU"
        timestamp depose_at "NOT NULL"
        timestamp modifie_at "NULL, dernier remplacement (RG15)"
    }

    relecture {
        bigint id PK "identity"
        bigint exercice_id FK "NOT NULL, UNIQUE -> exercice.id (RG8)"
        bigint relecteur_id FK "NOT NULL -> etudiant.id"
        integer note "NULL tant que non rendue, CHECK 0..20 (RG3)"
        varchar commentaire "NULL tant que non rendue, 2000"
        timestamp attribuee_at "NOT NULL"
        timestamp rendue_at "NULL tant que non rendue (RG11)"
    }
```

## Contraintes de la migration V1 et règles qu'elles protègent

| Table | Contrainte SQL | Règle |
|---|---|---|
| `session_cours` | `UNIQUE (code)` | Un code désigne une seule session |
| `presence` | `UNIQUE (session_id, etudiant_id)` | RG5 — une présence par étudiant et par session (`409 DEJA_PRESENT`) |
| `presence` | `CHECK (source IN ('ETUDIANT', 'FORMATEUR'))` | RG6 — source visible dans le tableau (Q14) |
| `exercice` | `UNIQUE (session_id, etudiant_id)` | RG12 — un exercice par étudiant et par session (`409 EXERCICE_DEJA_DEPOSE`) |
| `exercice` | `CHECK (statut IN ('DEPOSE', 'EN_ATTENTE_RELECTURE', 'RELU'))` | Cycle de vie, voir D4 |
| `relecture` | `UNIQUE (exercice_id)` | RG8 — un seul relecteur par exercice |
| `relecture` | `CHECK (note IS NULL OR note BETWEEN 0 AND 20)` | RG3 — note entière de 0 à 20 (le type `integer` interdit les décimales) |

Règles vérifiées **dans le service** et non en base, car elles dépendent de plusieurs tables ou de l'heure :
- RG2 et RG19 : `relecture.relecteur_id` différent de `exercice.etudiant_id`, et seul le relecteur attribué rend la relecture ;
- RG9 : le relecteur est choisi parmi les `presence` de la même `session_cours` ;
- RG1, RG4, RG16 : comparaison de l'heure courante avec `expiration_at` et `cloturee_at` ;
- RG20 : `etudiant.promotion_id` égal à `session_cours.promotion_id`.

Hors V1 : le blocage après cinq codes erronés (EF14, RG21, Could) demandera une migration supplémentaire s'il est réalisé.
