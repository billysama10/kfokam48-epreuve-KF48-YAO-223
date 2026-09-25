# Cahier des charges — KF48 Présences & Relectures

**Auteur :** KF48-YAO-223
**Version :** 1 · **Date :** 2026-09-25
**Frontend choisi :** React, parce que c'est l'écosystème le plus répandu et le mieux documenté, que Vite + TypeScript démarre en une commande, et que l'assistance IA y est la plus fiable.

---

## 1. Contexte et objectif

La formation KFOKAM48 suit aujourd'hui la présence de ses étudiants et les exercices rendus sans outil commun : la présence dépend d'un appel oral ou d'une feuille, les liens d'exercices circulent par messagerie, et la relecture entre pairs n'est ni attribuée ni tracée. Le formateur n'a donc aucune vue d'ensemble fiable de sa promotion.

L'application répond à ce problème avec un seul outil :
- le formateur ouvre une session de cours et obtient un **code de présence** valable 15 minutes ;
- chaque étudiant marque sa présence avec ce code, puis dépose le lien de son exercice ;
- le système attribue à chaque exercice **un relecteur tiré au hasard parmi les présents**, qui rend une note entière sur 20 et un commentaire ;
- le formateur consulte un **tableau par étudiant** : présences, exercices déposés, moyenne des notes reçues et relectures encore à faire.

Objectif mesurable : à la fin d'une séance, le formateur sait sans rien recompter qui était là, qui a rendu quoi, et quelles relectures bloquent.

## 2. Acteurs et rôles

| Acteur | Ce qu'il peut faire | Ce qu'il ne peut pas faire |
|---|---|---|
| Formateur | Ouvrir une session et obtenir son code ; ajouter une présence à la main ; clôturer une session ; consulter le tableau de sa promotion | Rendre une relecture ; modifier une note ; déposer un exercice à la place d'un étudiant |
| Étudiant | Choisir son nom dans la liste de sa promotion ; marquer sa présence avec un code ; déposer puis remplacer le lien de son exercice ; consulter la note et le commentaire reçus | Relire son propre exercice ; choisir son relecteur ; voir le nom de son relecteur ; marquer sa présence après expiration du code |
| Relecteur | Voir les exercices qui lui sont attribués ; rendre une note et un commentaire, une seule fois | Relire un exercice qui ne lui est pas attribué ; corriger une relecture déjà rendue |
| Système | Générer le code ; tirer au sort le relecteur ; calculer la moyenne | — |

**Le relecteur n'est pas un acteur distinct : c'est un étudiant dans un rôle.** Il n'existe pas de table « relecteur » : l'entité `Relecture` porte une référence `relecteur_id` vers `Etudiant`. Un même étudiant est à la fois auteur de son exercice et relecteur de l'exercice d'un pair. Le formateur est unique et n'a pas de compte (Q1) : il n'existe pas d'entité « formateur ».

## 3. Périmètre

**Inclus dans cette version :**
- Sélection de l'utilisateur dans une liste, sans mot de passe (Q1)
- Ouverture de session et génération d'un code de présence qui expire (Q2)
- Présence par code, et présence ajoutée par le formateur, marquée comme telle (Q14)
- Dépôt et remplacement du lien d'un exercice (Q12, Q13)
- Attribution automatique et aléatoire d'un relecteur parmi les présents (Q6, Q7)
- Relecture : note entière de 0 à 20 et commentaire, définitive (Q9, Q15)
- Clôture d'une session par le formateur (Q10, Q12)
- Tableau du formateur par promotion (Q11, Q16)
- Données de démonstration chargées au démarrage (une promotion, des étudiants)

**Explicitement exclu :**
- Authentification, mots de passe, comptes et droits d'accès (Q1 : « ne perdez pas de temps là-dessus »)
- Création et modification des promotions et des étudiants par l'interface : ils sont chargés par migration de démonstration
- Correction d'une note après envoi (tranché en section 7, Q10 contre Q15)
- Plusieurs relecteurs par exercice (Q6)
- Notifications, e-mails, rappels aux relecteurs en retard
- Plusieurs formateurs, plusieurs rôles d'administration
- Export (CSV, PDF) du tableau
- Soin visuel et CSS (non noté)
- Mise en ligne sur un serveur public : l'application tourne en local

## 4. Exigences fonctionnelles

| Réf | Exigence | Critère d'acceptation | Priorité |
|---|---|---|---|
| EF1 | L'utilisateur choisit son nom dans la liste de sa promotion (Q1) | Quand j'ouvre l'écran étudiant, alors je vois la liste des étudiants de la promotion ; quand je choisis mon nom, alors les écrans suivants agissent en mon nom sans mot de passe | Must |
| EF2 | Le formateur ouvre une session et obtient un code de présence | Quand j'envoie `POST /api/sessions` avec `titre` et `promotionId`, alors je reçois `201` avec `id`, `code`, `ouvertureAt` et `expirationAt = ouvertureAt + 15 min` ; quand un champ manque, alors je reçois `400` au format `{code, message}` | Must |
| EF3 | L'étudiant marque sa présence avec un code | Quand je saisis un code valide et non expiré, alors je reçois `201` avec `source = ETUDIANT` et ma présence apparaît dans le tableau du formateur ; code inconnu → `400 CODE_INCONNU` ; déjà présent → `409 DEJA_PRESENT` ; code expiré → `410 CODE_EXPIRE` | Must |
| EF4 | L'étudiant dépose le lien de son exercice pour une session | Quand je dépose un lien http(s) valide pour une session non clôturée, alors je reçois `201` avec `id` et `statut` ; lien invalide → `400 LIEN_INVALIDE` ; second dépôt pour la même session → `409 EXERCICE_DEJA_DEPOSE` | Must |
| EF5 | Le système attribue un relecteur à chaque exercice déposé (Q7) | Quand un exercice est déposé et qu'au moins un autre étudiant est présent à la session, alors un relecteur différent de l'auteur, présent à cette session, lui est attribué et le statut passe à `EN_ATTENTE_RELECTURE` ; quand aucun candidat n'existe, alors le statut reste `DEPOSE` | Must |
| EF6 | Le relecteur voit la liste des relectures qui lui sont attribuées | Quand j'ouvre l'écran relecteur après avoir choisi mon nom, alors je vois chaque exercice à relire avec son lien, la session concernée et l'`id` de la relecture ; les relectures déjà rendues sont marquées comme telles | Must |
| EF7 | Le relecteur rend une note et un commentaire | Quand j'envoie une note entière de 0 à 20 et un commentaire pour une relecture qui m'est attribuée, alors je reçois `200` et l'exercice passe à `RELU` ; note hors bornes ou non entière → `400 NOTE_INVALIDE` ; exercice dont je suis l'auteur → `403 AUTO_RELECTURE` ; relecture déjà rendue → `409 RELECTURE_DEJA_RENDUE` | Must |
| EF8 | Le formateur consulte le tableau de sa promotion (Q16) | Quand j'appelle `GET /api/tableau?promotionId=`, alors je reçois une ligne par étudiant avec `presences`, `exercicesDeposes`, `moyenne` (calculée par l'API, `null` sans note) et `relecturesEnAttente` ; promotion inconnue → `404 PROMOTION_INCONNUE` | Must |
| EF9 | Le formateur ajoute une présence à la main (Q14) | Quand j'ajoute un étudiant présent à une session non clôturée, même après expiration du code, alors la présence est créée avec `source = FORMATEUR` et le tableau l'indique « ajoutée par le formateur » ; déjà présent → `409 DEJA_PRESENT` | Should |
| EF10 | Le formateur clôture une session | Quand je clôture une session, alors tout dépôt, remplacement de lien, présence manuelle ou relecture sur cette session est refusé par `409 SESSION_CLOTUREE` | Should |
| EF11 | L'étudiant remplace le lien de son exercice (Q13) | Quand je remplace le lien d'un exercice dont la relecture n'est pas rendue et dont la session n'est pas clôturée, alors le nouveau lien est enregistré et le relecteur voit le nouveau lien ; sinon → `409 LIEN_NON_MODIFIABLE` | Should |
| EF12 | Le formateur voit le détail par session et les exercices en attente (Q11, Q16) | Quand j'ouvre le détail d'une session, alors je vois pour chaque étudiant s'il était présent (et la source), et chaque exercice `DEPOSE` ou `EN_ATTENTE_RELECTURE` est signalé comme « en attente » | Should |
| EF13 | L'étudiant consulte la note et le commentaire reçus (Q8) | Quand ma relecture est rendue, alors je vois la note et le commentaire, et aucune réponse de l'API destinée à l'étudiant ne contient l'identité du relecteur | Could |
| EF14 | Blocage après cinq codes erronés (Q4) | Quand un étudiant saisit cinq codes inconnus d'affilée, alors toute nouvelle tentative pendant 2 minutes est refusée par `400 TROP_DE_TENTATIVES`, même avec le bon code | Could |

## 5. Exigences non fonctionnelles

| Réf | Exigence | Comment on la vérifie |
|---|---|---|
| ENF1 | L'écran de marquage de présence est utilisable sur un téléphone (largeur 360 px) | Ouverture dans les outils développeur du navigateur en mode mobile 360 px : pas de défilement horizontal, champ et bouton accessibles |
| ENF2 | Le tableau du formateur répond en moins de 2 s pour une promotion de 60 étudiants et 20 sessions | Données de démonstration étendues à 60 étudiants ; temps de réponse relevé dans l'onglet Réseau du navigateur |
| ENF3 | Volumétrie cible : une promotion de 20 à 60 étudiants, jusqu'à 5 sessions par semaine | Hypothèse de dimensionnement ; une base H2 fichier suffit |
| ENF4 | Toute erreur renvoie `{ "code": "...", "message": "..." }`, jamais de stack trace ni de page d'erreur Spring | Test d'intégration MockMvc sur un cas d'erreur ; appel manuel avec un JSON mal formé |
| ENF5 | Les dates circulent en ISO-8601 et sont comparées côté serveur uniquement | Lecture du contrat ; l'expiration est décidée par l'API, jamais par l'horloge du téléphone |
| ENF6 | Chaque écran affiche un état de chargement et un message d'erreur lisible | Réseau ralenti dans le navigateur : l'indicateur de chargement apparaît ; API coupée : un message s'affiche |
| ENF7 | L'application démarre depuis un clone vierge en trois commandes au plus, avec des données de démonstration | README suivi à la lettre dans un dossier neuf |
| ENF8 | Les tests passent sans base locale installée | `./mvnw test` sur un poste vierge (H2 en mémoire) |
| ENF9 | Interface en français | Relecture des écrans et des messages d'erreur |

## 6. Règles de gestion

| Réf | Règle | Source |
|---|---|---|
| RG1 | Un code de présence expire 15 minutes après l'ouverture de la session ; à `expirationAt` exactement, il est déjà expiré | Q2 |
| RG2 | Un étudiant ne peut pas relire son propre exercice | Q5 |
| RG3 | Une note est un entier compris entre 0 et 20 inclus | Q9 |
| RG4 | La « fin de session » est l'expiration du code : après elle, aucune présence par code n'est acceptée (`410 CODE_EXPIRE`) | Q3, section 7 |
| RG5 | Un étudiant a au plus une présence par session (`409 DEJA_PRESENT`) | Contrat |
| RG6 | Une présence porte une source : `ETUDIANT` (par code) ou `FORMATEUR` (ajout manuel), affichée dans le tableau | Q14, contrat |
| RG7 | Le formateur peut ajouter une présence après l'expiration du code, tant que la session n'est pas clôturée | Q14, section 7 |
| RG8 | Un exercice a exactement un relecteur | Q6 |
| RG9 | Le relecteur est tiré au hasard parmi les étudiants présents à la session (toute source), auteur exclu ; à tirage égal, on privilégie le présent qui a le moins de relectures dans cette session | Q7, section 7 |
| RG10 | Si aucun candidat n'existe au moment du dépôt, l'exercice reste `DEPOSE` sans relecteur ; l'attribution est retentée à chaque nouvelle présence enregistrée dans la session | Section 7 (trou) |
| RG11 | Une relecture rendue est définitive : elle ne peut plus être modifiée (`409 RELECTURE_DEJA_RENDUE`) | Q15, arbitrage section 7 |
| RG12 | Un étudiant dépose au plus un exercice par session (`409 EXERCICE_DEJA_DEPOSE`) | Contrat |
| RG13 | Le dépôt est possible après la fin de session, jusqu'à la clôture, y compris pour un étudiant absent | Q12, section 7 |
| RG14 | Le lien d'un exercice est une URL absolue en `http` ou `https` (`400 LIEN_INVALIDE`) | Contrat |
| RG15 | Le lien peut être remplacé tant que la relecture n'est pas rendue et que la session n'est pas clôturée | Q13, section 7 |
| RG16 | Une session clôturée est figée : ni dépôt, ni remplacement, ni présence manuelle, ni relecture (`409 SESSION_CLOTUREE`) | Q10, Q12, section 7 |
| RG17 | L'identité du relecteur n'est jamais transmise à l'auteur de l'exercice | Q8 |
| RG18 | La moyenne d'un étudiant est la moyenne des notes de ses relectures rendues, arrondie à 2 décimales, `null` s'il n'en a aucune ; elle est calculée par l'API | Q16, F3, contrat |
| RG19 | Seul le relecteur attribué peut rendre une relecture : l'auteur reçoit `403 AUTO_RELECTURE`, tout autre étudiant `403 RELECTEUR_NON_ASSIGNE` | Q5, section 7 (trou) |
| RG20 | Un étudiant ne peut marquer sa présence ou déposer que pour une session de sa promotion (`400 ETUDIANT_HORS_PROMOTION`) | Hypothèse, section 7 |
| RG21 | Après cinq codes erronés consécutifs, un étudiant est bloqué 2 minutes (`400 TROP_DE_TENTATIVES`) | Q4 |

**Cycle de vie d'un exercice :** `DEPOSE` (sans relecteur) → `EN_ATTENTE_RELECTURE` (relecteur attribué) → `RELU` (relecture rendue). Le tableau considère `DEPOSE` et `EN_ATTENTE_RELECTURE` comme « en attente » (Q11).

**Cycle de vie d'une session :** `OUVERTE` (code valide) → `EXPIREE` (code expiré, calculé à partir de `expirationAt`) → `CLOTUREE` (action du formateur, possible à tout moment).

## 7. Zones d'ombre, hypothèses et contradictions

**Questions inutiles ou à faible valeur :**

| Question | Pourquoi elle apporte peu | Décision retenue | Conséquence |
|---|---|---|---|
| Q1 — mot de passe | Elle ne crée aucune fonctionnalité : elle confirme seulement qu'il n'y a rien à faire | Aucune authentification : l'utilisateur choisit son nom dans une liste | Authentification exclue du périmètre ; l'identité voyage dans les requêtes (voir « identité du relecteur ») |
| Q4 — blocage après 5 erreurs | Mesure anti-triche absente des cinq besoins du client et du contrat | Priorité Could (EF14, RG21). Réponse `400 TROP_DE_TENTATIVES` : on n'ajoute pas un statut HTTP (429) absent d'une opération imposée | Hors v0.1 et hors v1.0 sauf temps disponible |
| Q8 — l'étudiant voit sa note | Aucun des trois écrans imposés (F2) ni aucune opération du contrat ne l'exige | Priorité Could (EF13). En revanche RG17 s'applique dès la v0.1 : aucun DTO destiné à l'étudiant ne contient le relecteur | L'anonymat est garanti même si l'écran n'est jamais fait |

**Le trou que personne n'a vu :**

| Point | Réponse client (Qx) ou hypothèse | Décision retenue | Conséquence |
|---|---|---|---|
| « Fin de session » et « clôture » sont deux moments différents, jamais définis | Q3 parle de « fin de session », Q12 distingue « fin de session » et « clôture » par le formateur, Q2 ne connaît que l'expiration du code. Le contrat n'a aucune opération de clôture | Trois instants : **ouverture** (`ouvertureAt`) ; **fin de session = expiration du code** (`ouvertureAt + 15 min`), qui arrête les présences par code ; **clôture = action explicite du formateur**, qui fige tout le reste | RG4, RG7, RG13, RG16 ; ajout de l'opération `POST /api/sessions/{id}/cloture` au contrat ; colonne `cloturee_at` sur la session |
| Un étudiant dépose sans être présent | Q7 : relecteur parmi les présents. Q12 : dépôt possible après la séance. Rien ne dit si un absent peut déposer | Le dépôt est **autorisé** à tout étudiant de la promotion, mais un absent **n'entre pas dans le vivier de relecteurs** | RG13, RG9. Justification : le contrat de `POST /api/exercices` ne prévoit aucune erreur « absent » (seulement 400 et 409), et refuser le travail d'un étudiant pénaliserait l'apprentissage |
| Moins de deux présents : personne à qui attribuer la relecture | Q7 suppose qu'il existe toujours un autre présent. Avec 0 présent, ou l'auteur seul présent, le tirage est impossible | L'exercice reste `DEPOSE`, sans relecteur, affiché « en attente » (Q11) ; l'attribution est **retentée à chaque nouvelle présence** (par code ou par le formateur) | RG10 ; le formateur débloque la situation en ajoutant une présence (EF9) |

**Autres points que la demande ne tranche pas :**

| Point | Réponse client (Qx) ou hypothèse | Décision retenue | Conséquence |
|---|---|---|---|
| Qui envoie `POST /api/relectures/{id}` ? | Le corps ne contient que `note` et `commentaire` : sans identité, le `403 AUTO_RELECTURE` du contrat est invérifiable | Le frontend envoie l'en-tête `X-Etudiant-Id` (le nom choisi dans la liste, Q1). L'auteur reçoit `403 AUTO_RELECTURE`, un autre étudiant non attribué `403 RELECTEUR_NON_ASSIGNE` | RG19 ; RG2 est protégée deux fois : au tirage (RG9) et à l'envoi. Le corps imposé reste inchangé |
| Que veut dire « commencé à relire » ? (Q13) | Aucun événement « début de lecture » n'existe, et l'attribution est immédiate | « Commencé » = relecture rendue. Remplacement autorisé tant que la relecture n'est pas rendue et que la session n'est pas clôturée | RG15 ; ajout de `PUT /api/exercices/{id}` ; le relecteur voit toujours le lien courant |
| Tableau : « présence à chaque session » (Q16) contre `presences: integer` (contrat) | Le contrat ne donne qu'un total | Contrat inchangé : `presences` = nombre de sessions où l'étudiant est présent. Le détail par session et les exercices en attente passent par une opération complémentaire | EF12 (Should) ; ajout de `GET /api/sessions/{id}/detail` |
| « Relectures qu'il doit encore faire » (Q16) | Le contrat nomme le champ `relecturesEnAttente` | Nombre de relectures attribuées à l'étudiant et non rendues, toutes sessions confondues | Calcul dans le service du tableau |
| Mode de calcul de la moyenne | Q16 dit « moyenne des notes reçues » sans précision | Moyenne arithmétique des relectures rendues, 2 décimales, `null` si aucune | RG18 ; le frontend affiche « — » pour `null` sans recalculer |
| Identifiants inconnus dans les opérations imposées | Le contrat ne liste pas de 404 pour `POST /api/exercices` | Identifiant inconnu dans un corps → `400` (`SESSION_INCONNUE`, `ETUDIANT_INCONNU`), comme `CODE_INCONNU` ; relecture inconnue dans le chemin → `404 RELECTURE_INCONNUE`, ajout documenté au contrat | Aucun statut non prévu pour les corps de requête |
| Actions sur une session clôturée | Le contrat ne prévoit pas ce cas | `409 SESSION_CLOTUREE` (conflit avec l'état de la ressource) ; une présence par code sur une session clôturée reste `410 CODE_EXPIRE` si le code est expiré | RG16 |
| Étudiant d'une autre promotion | Non abordé | Refusé : `400 ETUDIANT_HORS_PROMOTION` | RG20 |
| Format du code | Non abordé | 6 caractères en majuscules et chiffres, unique parmi les sessions dont le code n'est pas expiré | Un code saisi désigne une seule session |
| Où sont créés promotions et étudiants ? | Non abordé | Chargés par une migration de démonstration, pas d'écran de gestion | Hors périmètre (section 3) |

**Contradictions relevées :**

| Réponses en conflit | Ce que j'ai choisi | Pourquoi |
|---|---|---|
| Q10 (« le relecteur peut corriger sa note tant que la session n'est pas clôturée ») contre Q15 (« une fois validée, c'est fini, il ne peut plus y revenir ») | **Q15 : la note est définitive dès l'envoi** (RG11) | 1. Le contrat imposé, non négociable (B2), prévoit `409 RELECTURE_DEJA_RENDUE` sur `POST /api/relectures/{id}` : un second envoi est un conflit, ce qui n'a de sens que si la note est figée. 2. Q15 est la seule réponse argumentée par le client (« plus honnête pour tout le monde »). 3. C'est plus simple : pas d'historique de notes, et une moyenne stable dans le tableau. La clôture (Q10) garde son rôle : elle arrête dépôts et relectures |

## 8. Contraintes techniques

**Imposées par le sujet :**

| # | Contrainte |
|---|---|
| B1 | Java 17 ou plus, Maven, wrapper `mvnw` commité |
| B2 | Contrat `api/contrat.yaml` respecté à la lettre : chemins, verbes, codes de statut, format d'erreur |
| B3 | Couches contrôleur / service / repository ; aucune requête base dans un contrôleur ; aucune entité JPA exposée, uniquement des DTO |
| B4 | Validation des entrées et gestion centralisée des erreurs par `@RestControllerAdvice`, réponse toujours `{ "code", "message" }` |
| B5 | Schéma versionné par migrations, commitées ; `ddl-auto=update` interdit hors tests |
| B6 | Un test unitaire sur une règle métier réelle et un test d'intégration sur un endpoint, qui tournent sans base locale |
| F1 | Framework déclaré et justifié en une ligne dans le README, build qui passe |
| F2 | Trois écrans : formateur, étudiant, relecteur |
| F3 | Appels API dans une couche dédiée, états de chargement et d'erreur, aucune règle métier dupliquée (la moyenne vient de l'API) |
| — | Démarrage par `docker compose up` ou trois commandes au plus, avec données de démonstration |

**Choix que je m'impose :**
- Backend : Java 21, Spring Boot 3, Maven avec wrapper.
- Base : H2 en mode fichier pour la démonstration (aucune installation nécessaire), H2 en mémoire pour les tests.
- Migrations : Flyway (`V1__schema.sql`, `V2__donnees_demo.sql`…), `spring.jpa.hibernate.ddl-auto=validate` ; le diagramme D2 est tenu identique aux migrations.
- Tests : JUnit 5 pour le test unitaire (RG1, expiration du code, avec une horloge injectée), MockMvc pour le test d'intégration (`POST /api/presences` : 201, 409, 410).
- Temps : une `Clock` injectée dans les services pour rendre l'expiration testable.
- Frontend : React + Vite + TypeScript, appels API regroupés dans `src/api/`.
- Contrat : opérations ajoutées dans `api/contrat.yaml` et figées avant le premier commit de code : liste des promotions et des étudiants, clôture, présence manuelle, remplacement du lien, relectures d'un étudiant, détail d'une session.
- Git : une branche par issue (`feature/` ou `fix/` suivi du numéro et d'un mot-clé, par exemple `feature/3-marquer-presence`), une PR par branche, un commit qui ferme l'issue en la citant (par exemple `Enregistrement d'une présence par code (RG1) — Closes #3`), aucun `push --force`, `.gitignore` Java + JS déjà en place.

## 9. Livrables

- `docs/CAHIER_DES_CHARGES.md` (ce document), tenu à jour après l'étape 3
- `docs/JOURNAL.md`, une entrée par étape
- `docs/diagrammes/` en Mermaid : D1 cas d'utilisation, D2 modèle de données (identique aux migrations), D3 séquence « marquer sa présence » (201, 409, 410), D4 bonus états-transitions d'un exercice
- Backlog d'une dizaine d'issues GitHub : titre qui décrit un résultat utilisateur, critères « quand … alors … », priorité Must / Should / Could, renvoi aux EFx / RGx, estimation
- `api/contrat.yaml` complété et figé avant le premier commit de code
- `/backend` : application Spring Boot, migrations Flyway, tests
- `/frontend` : application React avec les trois écrans
- `README.md` : démarrage en trois commandes au plus, justification du frontend, testé depuis un clone vierge
- `CHANGELOG.md` cohérent avec l'historique
- Les trois commits de jalon : `[JALON] analyse`, `[JALON] v0.1`, `[JALON] v1.0`
- Un seul dépôt GitHub public : `kfokam48-epreuve-KF48-YAO-223`
- `SOUMISSION.md` déposé sur la plateforme avant 18h00, avec le hash complet du commit final

## 10. Démarche prévue

1. **Analyse (étape 1)** : ce cahier des charges, puis les diagrammes D1 à D3 (et D4), le contrat complété, les issues. Commits de documentation sur `main`, puis `[JALON] analyse` poussé. Aucun code avant ce jalon.
2. **Première version (étape 2)** : **seules les EF Must sont livrées en v0.1** (EF1 à EF8). Ordre : squelette backend + migrations, sessions (EF2), présences (EF3), exercices et attribution (EF4, EF5), relectures (EF6, EF7), tableau (EF8), puis les trois écrans. Une branche et une PR par issue, puis `[JALON] v0.1` poussé.
3. **Enveloppe (étape 3)** : demande de l'enveloppe au surveillant dès `[JALON] v0.1` poussé, puis une issue pour le bug et une pour l'évolution avant tout code ; bug reproduit par un test ; nouvelle migration Flyway (jamais de modification d'une migration existante) ; contrat mis à jour ; re-priorisation écrite dans le journal ; correctif et évolution sur deux branches séparées ; ce document et les diagrammes corrigés dans un commit qui le dit.
4. **Version finale (étape 4)** : `[JALON] v1.0`, `CHANGELOG.md`, README testé depuis un clone vierge, backlog restant trié. **Les EF Should puis Could ne sont traitées que si le temps le permet, après le jalon v1.0.**
5. **Soumission (étape 5)** : dernier commit poussé, hash complet sur 40 caractères vérifié, lien du dépôt testé en navigation privée, `SOUMISSION.md` déposé bien avant 18h00.

**En cas de retard :** on coupe d'abord les Could, puis les Should ; les Must, les jalons et le journal ne sont jamais sacrifiés. La soumission passe avant toute fonctionnalité.

**Definition of Done — une issue est terminée quand :**
- tous ses critères d'acceptation sont vérifiés, et les réponses d'erreur respectent le contrat et le format `{code, message}` ;
- le code est sur une branche dédiée, fusionnée dans `main` par une PR contenant `Closes #` suivi du numéro de l'issue ;
- les messages de commit citent l'EFx / RGx concernée et le numéro d'issue ;
- `./mvnw test` passe et le build du frontend passe ;
- si le schéma change : une nouvelle migration Flyway, et D2 mis à jour ;
- si l'API change : `api/contrat.yaml` mis à jour dans la même PR.

---

## Journal des révisions

| Version | Quand | Ce qui a changé et pourquoi |
|---|---|---|
| 1 | 2026-09-25, étape 1 | Version initiale : 14 EF, 21 RG, contradiction Q10/Q15 tranchée en faveur de Q15, trou « fin de session / clôture / absent / moins de 2 présents » comblé |
| 1.1 | 2026-09-25, étape 1 | Alignement sur le sujet mis à jour (11h52) : démarche en 5 étapes (épreuve Git supprimée, soumission en étape 5), un seul dépôt, « ticket » remplacé par « issue », branches `feature/`, enveloppe remise par le surveillant, issues avec estimation |
