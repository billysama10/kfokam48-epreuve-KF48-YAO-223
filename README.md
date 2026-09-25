# KF48 Présences et Relectures

Application de la formation KFOKAM48 : le formateur ouvre une session et obtient un **code de présence**, les étudiants marquent leur présence et déposent le lien de leur exercice, chaque exercice est relu par un pair tiré au sort, et le formateur suit tout dans un **tableau**.

Analyse complète : [cahier des charges](docs/CAHIER_DES_CHARGES.md), [diagrammes](docs/diagrammes/), [contrat d'API](api/contrat.yaml), [journal](docs/JOURNAL.md).

## Choix du frontend

**React**, parce que c'est l'écosystème le plus répandu et le mieux documenté, que Vite + TypeScript démarre en une commande, et que l'assistance IA y est la plus fiable.

## Prérequis

- **Java 21 obligatoire** : la variable **JAVA_HOME** doit pointer vers un JDK 21, sinon mvnw refuse de compiler. Si un autre JDK est installé, régler JAVA_HOME avant de lancer le backend, par exemple sous PowerShell :

```powershell
$env:JAVA_HOME = "C:\Program Files\Eclipse Adoptium\jdk-21.0.12.101-hotspot"
```

- **Node.js 22** ou plus récent (exigé par Vite 8)
- Aucune base de données à installer : le backend utilise **H2** en mode fichier (dossier backend/data, créé au premier démarrage)

## Démarrage en trois commandes

Depuis la racine du dépôt, dans deux terminaux :

```bash
cd backend && ./mvnw spring-boot:run
```

```bash
cd frontend && npm install
npm run dev
```

Sous Windows (invite de commandes), la première commande devient :

```bat
cd backend && mvnw.cmd spring-boot:run
```

Puis ouvrir **http://localhost:5173**. Le backend répond sur **http://localhost:8080**.

## Démarrage avec Docker

```bash
docker compose up --build
```

Puis ouvrir **http://localhost:5173**.

## Données de démonstration

Chargées automatiquement au premier démarrage par la migration **V2** (dossier backend/src/main/resources/db/demo) :

- la promotion **KF48 Yaoundé 2026** et six étudiants ;
- une session passée, **Introduction à Spring Boot** (code **DEMO01**, expiré), avec cinq présents dont une présence ajoutée par le formateur ;
- quatre exercices déposés : deux relus (notes 15 et 12), deux en attente de relecture.

Pour repartir de zéro, arrêter le backend et supprimer le dossier backend/data.

## Tests

```bash
cd backend && ./mvnw test
```

Les tests utilisent **H2 en mémoire** : aucune base locale n'est nécessaire.

## Structure

| Dossier | Contenu |
|---|---|
| backend | Spring Boot 3.5, Java 21, Maven (wrapper mvnw), Flyway, H2 |
| frontend | React + Vite + TypeScript, appels API regroupés dans src/api |
| api | Contrat OpenAPI **contrat.yaml** |
| docs | Cahier des charges, journal, diagrammes Mermaid |
