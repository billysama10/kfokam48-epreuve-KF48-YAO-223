# KF48 Présences et Relectures

Application de la formation KFOKAM48 : le formateur ouvre une session et obtient un **code de présence** valable 15 minutes, les étudiants marquent leur présence et déposent le lien de leur exercice, chaque exercice est relu par **deux pairs** tirés au sort parmi les présents, et le formateur suit tout dans un **tableau** (présences, exercices, moyenne, relectures à faire).

Analyse complète : [cahier des charges](docs/CAHIER_DES_CHARGES.md), [diagrammes](docs/diagrammes/), [contrat d'API](api/contrat.yaml), [journal](docs/JOURNAL.md), [historique des versions](CHANGELOG.md).

## Choix du frontend

**React**, parce que c'est l'écosystème le plus répandu et le mieux documenté, que Vite + TypeScript démarre en une commande, et que l'assistance IA y est la plus fiable.

## Prérequis

- **Java 21 obligatoire** : la variable **JAVA_HOME** doit pointer vers un JDK 21, sinon mvnw refuse de compiler. Si un autre JDK est installé, régler JAVA_HOME avant de lancer le backend, par exemple sous PowerShell :

```powershell
$env:JAVA_HOME = "C:\Program Files\Eclipse Adoptium\jdk-21.0.12.101-hotspot"
```

- **Node.js 20.19 ou plus récent** (ou 22.12 et plus) : c'est la version minimale exigée par Vite 8
- Aucune base de données à installer : le backend utilise **H2** en mode fichier (dossier backend/data, créé au premier démarrage)
- Maven n'est pas nécessaire : le wrapper **mvnw** est fourni

## Démarrage en trois commandes

Depuis la racine du dépôt, dans deux terminaux :

```bash
cd backend && ./mvnw spring-boot:run
```

```bash
cd frontend && npm install
npm run dev
```

Sous Windows (PowerShell ou invite de commandes), la première commande devient :

```bat
cd backend && mvnw.cmd spring-boot:run
```

Puis ouvrir **http://localhost:5173**. Le backend répond sur **http://localhost:8080** ; le frontend lui relaie tous les appels **/api**.

**Ports déjà occupés ?** Le backend accepte un autre port, et le frontend une autre cible :

```bash
cd backend && ./mvnw spring-boot:run -Dspring-boot.run.arguments=--server.port=18080
cd frontend && API_CIBLE=http://localhost:18080 npx vite --port 5174
```

Sous PowerShell, la variable s'écrit ainsi :

```powershell
cd frontend; $env:API_CIBLE = "http://localhost:18080"; npx vite --port 5174
```

## Démarrage avec Docker

```bash
docker compose up --build
```

Puis ouvrir **http://localhost:5173**. Ce mode n'a pas pu être testé sur le poste de développement (démon Docker arrêté) : les trois commandes ci-dessus sont la voie de référence.

## Données de démonstration

Chargées automatiquement au premier démarrage par la migration **V2** (dossier backend/src/main/resources/db/demo), puis conservées par **V3**. Promotion **KF48 Yaoundé 2026**, session passée **Introduction à Spring Boot** (code **DEMO01**, expiré depuis le 24/09/2026) :

| Étudiant | Présence à DEMO01 | Son exercice | Ce qu'il doit relire |
|---|---|---|---|
| **Awa Ngono** | présente (code) | relu par Boris : **15**, définitive | l'exercice de Carine (à faire) |
| **Boris Kamga** | présent (code) | relu par Carine : **12**, définitive | l'exercice d'Awa (rendu) |
| **Carine Mbarga** | présente (code) | en attente de relecture (Awa) | l'exercice de Boris (rendu) |
| **David Fotso** | présent (code) | en attente de relecture (Esther) | — |
| **Esther Nana** | **ajoutée par le formateur** | pas de dépôt | l'exercice de David (à faire) |
| **Franck Tchinda** | absent | pas de dépôt | — |

Les deux notes de 15 et 12 ont été rendues avant le passage à deux relecteurs : elles restent définitives (pas d'effet rétroactif, cahier des charges RG23).

**Parcours conseillé** :
1. Onglet **Formateur** : ouvrir une session et noter le code.
2. Choisir « Je suis » **Awa**, onglet **Étudiant** : saisir le code, puis déposer un lien pour cette session. L'exercice reste **DEPOSE**, car personne d'autre n'est présent.
3. Faire de même avec **Boris** puis **Carine** (présence seulement) : l'exercice d'Awa reçoit ses deux relecteurs.
4. Onglet **Relecteur** avec Boris, puis Carine : rendre une note. Dans le tableau, la moyenne d'Awa est d'abord marquée **(provisoire)**, puis définitive.

Pour repartir de zéro, arrêter le backend et supprimer le dossier backend/data. Une collection **Bruno** est fournie dans api/bruno (présence : 201, 409, 410, 400).

## Tests

```bash
cd backend && ./mvnw test
```

41 tests, tous sur **H2 en mémoire** : aucune base locale n'est nécessaire. Les deux tests imposés sont :
- **PresenceServiceTest** : test unitaire de RG1, le code expire à 15 minutes pile (horloge figée) ;
- **PresenceControllerIntegrationTest** : test d'intégration MockMvc de **POST /api/presences** (201, 409, 410, 400).

On y trouve aussi le test concurrent du bug #26 (**PresenceConcurrenceTest**) et le test de conservation des données par la migration V3 (**MigrationV3Test**).

## Écarts par rapport au cahier des charges et au sujet

- **Contrat** : l'opération imposée **GET /api/tableau** a reçu un champ supplémentaire, **moyenneProvisoire**, à la suite du changement de besoin de l'étape 3 ; ses champs imposés sont inchangés.
- **Relecture** : le relecteur s'identifie par l'en-tête **X-Etudiant-Id** sur **POST /api/relectures/{id}**, sans quoi le **403 AUTO_RELECTURE** du contrat serait invérifiable (cahier des charges, section 7).
- **Authentification** : aucune, conformément à Q1 ; chacun choisit son nom dans une liste.
- **Docker** : fichier docker-compose.yml fourni mais non testé (voir plus haut).

## Fonctionnalités reportées après v1.0

Reportées à l'étape 3 pour absorber le bug et le changement de besoin (cahier des charges, section 10), étiquetées « Reporté » dans les issues :

| Issue | Fonctionnalité | Priorité | Conséquence tant qu'elle manque |
|---|---|---|---|
| #9 | Présence ajoutée à la main par le formateur | Should | Il faut trois présents pour qu'un exercice ait ses deux relecteurs |
| #10 | Clôture d'une session | Should | Une session n'est jamais figée ; dépôts et relectures tardifs restent possibles |
| #11 | Remplacement du lien d'un exercice | Should | Un lien erroné ne peut pas être corrigé |
| #12 | Détail d'une session et exercices en attente | Should | Le formateur ne voit que le tableau par étudiant |
| #13 | Note vue par l'étudiant, sans le relecteur | Could | L'étudiant ne voit pas encore sa note |
| #14 | Blocage après cinq codes erronés | Could | Aucune limite de tentatives |

## Structure

| Dossier | Contenu |
|---|---|
| backend | Spring Boot 3.5, Java 21, Maven (wrapper mvnw), Flyway (V1 schéma, V2 démonstration, V3 deux relecteurs), H2 |
| frontend | React + Vite + TypeScript, appels API regroupés dans src/api |
| api | Contrat OpenAPI **contrat.yaml** et collection Bruno |
| docs | Cahier des charges, journal, diagrammes Mermaid |
