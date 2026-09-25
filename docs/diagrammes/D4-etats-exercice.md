# D4 — États-transitions d'un exercice (bonus)

Cycle de vie de la colonne **exercice.statut** (voir D2) : **déposé → en attente de relecture → relu**. Les valeurs sont celles renvoyées par l'API dans le champ **statut**.

Version 2 (étape 3) : **conséquence du changement de besoin double relecture** — deux relecteurs par exercice, **RELU** seulement quand les deux ont rendu, note provisoire entre les deux.

Règles citées : RG8, RG9, RG10, RG22 (attribution), RG11 (relecture définitive), RG23 (note provisoire), RG12, RG13, RG14 (dépôt), RG15 (remplacement du lien), RG16 (clôture).

```mermaid
stateDiagram-v2
    direction LR

    [*] --> choix : dépôt du lien (EF4, RG12, RG13, RG14)

    state choix <<choice>>
    choix --> EN_ATTENTE_RELECTURE : au moins un autre présent, un ou deux relecteurs tirés (RG8, RG9)
    choix --> DEPOSE : aucun candidat (RG10)

    DEPOSE : DEPOSE
    DEPOSE : déposé, sans relecteur
    DEPOSE : « en attente » au tableau (Q11)

    EN_ATTENTE_RELECTURE : EN_ATTENTE_RELECTURE
    EN_ATTENTE_RELECTURE : un ou deux relecteurs, les deux notes pas encore rendues
    EN_ATTENTE_RELECTURE : après la première note, note provisoire (RG23)
    EN_ATTENTE_RELECTURE : « en attente » au tableau (Q11)

    RELU : RELU
    RELU : les deux relectures rendues, note = moyenne
    RELU : définitif (RG11)

    DEPOSE --> DEPOSE : remplacement du lien (RG15)
    DEPOSE --> EN_ATTENTE_RELECTURE : nouvelle présence, relecteur tiré (RG10)
    EN_ATTENTE_RELECTURE --> EN_ATTENTE_RELECTURE : second relecteur tiré à une nouvelle présence (RG10)
    EN_ATTENTE_RELECTURE --> EN_ATTENTE_RELECTURE : première relecture rendue, note provisoire (RG23)
    EN_ATTENTE_RELECTURE --> EN_ATTENTE_RELECTURE : remplacement du lien (RG15)
    EN_ATTENTE_RELECTURE --> RELU : seconde relecture rendue (EF7, RG3, RG19)
    RELU --> [*]
```

## Données antérieures au changement

Un exercice déjà **RELU** avec une seule relecture avant la migration **V3** reste **RELU** et sa note reste définitive (RG23, pas d'effet rétroactif).

## Effet de la clôture (RG16)

La clôture d'une session **ne change pas le statut** de ses exercices : elle les **fige**. Après clôture, aucune transition n'est plus possible (ni remplacement, ni attribution, ni relecture, réponse **409 SESSION_CLOTUREE**). Un exercice resté **DEPOSE** ou **EN_ATTENTE_RELECTURE** apparaît définitivement « en attente » dans le tableau du formateur (Q11).

| Transition | Déclencheur | Refusée si | Règle |
|---|---|---|---|
| → **DEPOSE** ou **EN_ATTENTE_RELECTURE** | **POST /api/exercices** | lien invalide (**400**), déjà déposé (**409**), session clôturée (**409**) | RG12, RG13, RG14, RG16 |
| **DEPOSE** → **EN_ATTENTE_RELECTURE** | nouvelle présence dans la session | session clôturée | RG10, RG16 |
| remplacement du lien | **PUT /api/exercices/{id}** | relecture rendue ou session clôturée (**409**) | RG15, RG16 |
| **EN_ATTENTE_RELECTURE** → **RELU** | **POST /api/relectures/{id}** de la seconde relecture (la première laisse l'exercice en attente, note provisoire) | note invalide (**400**), auto-relecture (**403**), déjà rendue (**409**), session clôturée (**409**) | RG2, RG3, RG11, RG16, RG19 |
