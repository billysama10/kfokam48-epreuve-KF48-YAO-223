# D1 — Cas d'utilisation

Acteurs et ce que chacun peut faire. Chaque cas renvoie à son exigence (EFx) et à ses règles (RGx) du [cahier des charges](../CAHIER_DES_CHARGES.md).

Le **relecteur n'est pas un acteur distinct** : c'est un étudiant dans un rôle (section 2 du cahier des charges). Le lien « est un » le montre.

```mermaid
flowchart LR
    F(["Formateur"])
    E(["Étudiant"])
    R(["Relecteur<br/>rôle d'un étudiant"])
    S(["Système"])

    subgraph APP["Application KF48 Présences & Relectures"]
        direction TB
        UC1["EF1 · Choisir son nom dans la liste<br/>Must · Q1"]
        UC2["EF2 · Ouvrir une session et obtenir un code<br/>Must · RG1"]
        UC3["EF3 · Marquer sa présence avec un code<br/>Must · RG1 RG4 RG5 RG6 RG20"]
        UC4["EF4 · Déposer le lien de son exercice<br/>Must · RG12 RG13 RG14 RG16"]
        UC5["EF5 · Attribuer un relecteur au hasard<br/>Must · RG2 RG8 RG9 RG10"]
        UC6["EF6 · Voir ses relectures à faire<br/>Must"]
        UC7["EF7 · Rendre une note et un commentaire<br/>Must · RG2 RG3 RG11 RG16 RG19"]
        UC8["EF8 · Consulter le tableau de la promotion<br/>Must · RG18"]
        UC9["EF9 · Ajouter une présence à la main<br/>Should · RG5 RG6 RG7 RG16"]
        UC10["EF10 · Clôturer une session<br/>Should · RG16"]
        UC11["EF11 · Remplacer le lien de son exercice<br/>Should · RG14 RG15 RG16"]
        UC12["EF12 · Voir le détail d'une session<br/>Should · RG6"]
        UC13["EF13 · Voir la note reçue, sans le relecteur<br/>Could · RG17"]
        UC14["EF14 · Bloquer après cinq codes erronés<br/>Could · RG21"]
    end

    R -. "est un" .-> E

    F --- UC2
    F --- UC8
    F --- UC9
    F --- UC10
    F --- UC12

    E --- UC1
    E --- UC3
    E --- UC4
    E --- UC11
    E --- UC13

    R --- UC6
    R --- UC7

    S --- UC5
    S --- UC14

    UC4 -. "«include»" .-> UC5
    UC3 -. "«include» retentative RG10" .-> UC5
    UC9 -. "«include» retentative RG10" .-> UC5
    UC14 -. "«extend»" .-> UC3
```

## Lecture

| Acteur | Cas d'utilisation | Remarque |
|---|---|---|
| Formateur | EF2, EF8, EF9, EF10, EF12 | Pas de compte, formateur unique (Q1) |
| Étudiant | EF1, EF3, EF4, EF11, EF13 | Identité choisie dans une liste, sans mot de passe (Q1) |
| Relecteur | EF6, EF7 (et tout ce que fait un étudiant) | Rôle porté par l'entité `relecture` (colonne `relecteur_id`), voir D2 |
| Système | EF5, EF14 | Le tirage est déclenché par un dépôt (EF4) ou par toute nouvelle présence (EF3, EF9) quand des exercices attendent un relecteur (RG10) |
