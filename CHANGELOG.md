# Historique des versions

Format inspiré de « Keep a Changelog ». Chaque ligne cite l'issue et la pull request (PR) qui l'ont apportée, sur le dépôt **billysama10/kfokam48-epreuve-KF48-YAO-223**. Les versions correspondent aux commits de jalon **[JALON] v0.1** et **[JALON] v1.0**.

## v1.0 — 2026-09-25

Version finale : correction du bug signalé par le client et changement de besoin de l'enveloppe (étape 3).

### Corrigé
- Deux étudiants qui saisissent le code en même temps sont désormais tous les deux enregistrés : la seconde présence était annulée avec une erreur 500 quand un exercice attendait un relecteur. Verrou d'écriture sur la session ; un double envoi renvoie **409 DEJA_PRESENT** au lieu de 500. Test concurrent ajouté avant la correction — issue #26, PR #27

### Modifié
- Chaque exercice est relu par **deux pairs différents** présents à la session, au lieu d'un seul (RG8 remplace Q6) ; la liste se complète à chaque nouvelle présence, et l'exercice n'est **RELU** qu'après les deux rendus. Migration **V3** (contrainte UNIQUE (exercice_id, relecteur_id)), données existantes conservées, V1 et V2 intactes — issue #28, PR #30
- La note d'un exercice est la **moyenne de ses deux relectures**, marquée **provisoire** tant qu'une seule est rendue ; le tableau reçoit le champ **moyenneProvisoire** (seul ajout à une opération imposée) et affiche « (provisoire) » — issue #29, PR #31
- Cahier des charges v2 et diagrammes D1 à D4 v2, conséquence du changement de besoin ; re-priorisation : EF9 à EF14 reportées après v1.0 — issues #28 et #29, PR #30
- README final : prérequis (Java 21, Node.js 20.19+), démarrage, données de démonstration, tests, écarts et reports ; testé depuis un clone vierge — issue #34, PR #35

### Ajouté
- Ce CHANGELOG — issue #33, PR #36
- Journal des étapes 3 et 4 — PR #32 et PR #37

### Reporté après v1.0
- Présence ajoutée à la main (#9), clôture d'une session (#10), remplacement du lien (#11), détail d'une session (#12), note vue par l'étudiant (#13), blocage après cinq codes erronés (#14) : étiquetées « Reporté », voir le cahier des charges, section 10

## v0.1 — 2026-09-25

Première version : les huit exigences **Must** (EF1 à EF8).

### Ajouté
- Socle : backend Spring Boot 3.5 / Java 21 avec wrapper mvnw, schéma **V1** identique au diagramme D2, données de démonstration **V2**, erreurs toujours au format { code, message }, frontend React + Vite + TypeScript, README et docker compose — issue #15, PR #16
- Choix de sa promotion et de son nom dans une liste, sans mot de passe (EF1) — issue #1, PR #17
- Ouverture d'une session avec un code de 6 caractères qui expire 15 minutes après l'ouverture (EF2, RG1) — issue #2, PR #18
- Présence par code : **400 CODE_INCONNU**, **410 CODE_EXPIRE**, **409 DEJA_PRESENT** ; les deux tests imposés (unitaire RG1, intégration MockMvc) et la collection Bruno (EF3) — issue #3, PR #19
- Dépôt du lien d'un exercice, possible après la fin de session (EF4, RG13, RG14) — issue #4, PR #20
- Attribution automatique d'un relecteur présent, jamais l'auteur (EF5, RG2, RG9, RG10) — issue #5, PR #21
- Liste des relectures à faire (EF6) — issue #6, PR #22
- Rendu d'une note entière de 0 à 20 et d'un commentaire, définitif (EF7, RG3, RG11) — issue #7, PR #23
- Tableau du formateur : présences, exercices, moyenne calculée par l'API, relectures en attente (EF8, RG18) — issue #8, PR #24
- Journal de l'étape 2 — PR #25

### Analyse (avant le jalon analyse)
- Cahier des charges v1 (14 EF, 21 RG, contradiction Q10/Q15 tranchée), diagrammes D1 à D4 en Mermaid, contrat d'API complété (9 opérations ajoutées aux 5 imposées), backlog de 14 issues, journal de l'étape 1 — commits sur main pendant l'étape 1, jusqu'à **[JALON] analyse**
