# Règles du projet — Épreuve KFOKAM48 (candidat KF48-YAO-223, GitHub billysama10)

Le sujet complet est dans SUJET.md et CLIENT.md (à la racine du dépôt, dossier `sujet/`). Relis-les avant chaque étape.
Le candidat n'est pas développeur : explique en 3 lignes max ce que tu fais, en français.

## Ce qui est noté (priorité absolue)
- Analyse (38 pts) AVANT tout code. Le commit `[JALON] analyse` doit précéder le premier commit de code.
- Git (32 pts) : commits atomiques, messages clairs en français citant EFx/RGx et `#issue`.
- Jalons exacts : `git commit --allow-empty -m "[JALON] analyse"`, puis `[JALON] v0.1`, puis `[JALON] v1.0`, poussés.

## Règles Git NON négociables
- Jamais de commit direct de code sur main. Une branche par issue : `feat/<num>-<slug>` ou `fix/<num>-<slug>`.
- Une PR par branche via `gh pr create`, corps contenant `Closes #<num>`, puis `gh pr merge --merge --delete-branch`.
- Jamais `push --force` sur ce dépôt. Jamais commiter target/, node_modules/, dist/, build/, .env, secrets.
- Push après chaque commit.
- Les docs d'analyse (docs/) peuvent être commitées sur main pendant l'étape 1 seulement.

## Stack imposée
- /backend : Spring Boot 3, Java 21, Maven + wrapper mvnw commité, H2 fichier ou PostgreSQL via docker compose, Flyway (ddl-auto=validate), DTO, couches controller/service/repository, @RestControllerAdvice renvoyant TOUJOURS `{ "code": "...", "message": "..." }`.
- Tests : 1 unitaire sur une RG réelle + 1 intégration (MockMvc) ; doivent tourner sans base locale (H2 en test).
- /frontend : React + Vite + TypeScript, appels API dans `src/api/` uniquement, états loading/erreur, la moyenne vient de l'API.
- /api/contrat.yaml : respect à la lettre des 5 opérations imposées.
- /docs : CAHIER_DES_CHARGES.md, JOURNAL.md, diagrammes/ en Mermaid (D1 cas d'utilisation, D2 classes = migrations, D3 séquence présence avec 201/409/410, D4 états exercice en bonus).
- Démarrage : `docker compose up` ou 3 commandes max dans le README, avec données de démo.

## Journal
Après chaque étape, ajoute une entrée dans docs/JOURNAL.md (Fait / Bloqué + durée / IA + vérification) et commite-la immédiatement.
