# Journal de bord — KF48-YAO-223

> Une entrée par étape, écrite au moment où elle se termine.
> Chaque entrée répond à trois questions : **Fait**, **Bloqué** (et combien de temps), **IA** (ce que je lui ai demandé et comment j'ai vérifié).

---

## Étape 1 — Analyse et conception

**Fait :** cahier des charges complet (10 sections, 14 exigences fonctionnelles, 21 règles de gestion, contradiction Q10/Q15 et trou tranchés en section 7) ; 4 diagrammes en Mermaid (D1 cas d'utilisation, D2 modèle de données aligné sur la future migration V1, D3 séquence « marquer sa présence », D4 bonus états d'un exercice) ; contrat d'API complété avec 9 opérations, les 5 imposées étant inchangées ; 14 issues créées sur GitHub (#1 à #8 en **Must**, #9 à #12 en **Should**, #13 et #14 en **Could**) ; jalon **[JALON] analyse** posé juste après cette entrée.

**Bloqué :** environ 2 h 20 au total pour l'étape (de 10h58 à 13h20 d'après l'historique Git), dont :
- la contradiction Q10/Q15, tranchée pour **Q15** (note définitive) grâce au **409 RELECTURE_DEJA_RENDUE** du contrat imposé, qui n'a de sens que si une relecture rendue ne peut plus changer ;
- le rendu des diagrammes Mermaid, impossible sans Chrome sur le poste (Edge a refusé de démarrer en mode automatique) : j'ai contrôlé la syntaxe avec l'analyseur Mermaid, sans rendu visuel ;
- une erreur d'accolades dans le contrat : un texte contenant **{id}** cassait la lecture du fichier, corrigé en le mettant entre guillemets.

**IA :** elle a rédigé le cahier des charges, les diagrammes, le contrat et les issues à partir du sujet et de CLIENT.md, et m'a présenté son plan des contradictions avant d'écrire. J'ai modifié la justification de React (écosystème le plus répandu et le mieux documenté, démarrage en une commande avec Vite, assistance IA fiable, au lieu de « le framework que je maîtrise ») et le périmètre (seules les EF **Must** en v0.1, **Should** et **Could** seulement après v1.0 si le temps le permet). Vérification : le contrat passe le validateur OpenAPI et la comparaison avec la version imposée ne montre que des ajouts ; les 4 diagrammes passent l'analyseur Mermaid ; j'ai relu chaque critère d'acceptation pour vérifier qu'il est formulé « quand … alors … » et qu'il cite un code HTTP ou un comportement observable.

---

## Étape 2 — Première version

**Fait :** les 8 issues **Must** (#1 à #8) plus l'issue socle #15, chacune sur sa branche, fusionnée par sa PR (9 PR, #16 à #24), toutes les issues fermées. Backend Spring Boot 3.5 / Java 21 avec les 5 opérations imposées du contrat et 4 opérations ajoutées (promotions, étudiants, sessions d'une promotion, relectures d'un étudiant) ; schéma **V1** identique à D2 et données de démonstration **V2** séparées ; erreurs toujours au format **{ code, message }**. Frontend React avec les trois écrans (formateur : session et tableau ; étudiant : présence et dépôt ; relecteur : liste et note). **34 tests** verts sur H2 en mémoire, dont les deux imposés : unitaire **PresenceServiceTest** (RG1, expiration à 15 minutes pile) et intégration MockMvc **PresenceControllerIntegrationTest** (201, 409, 410, 400 comme D3). Collection Bruno dans api/bruno. Jalon **[JALON] v0.1** posé juste après cette entrée.

**Bloqué :** environ 1 h 45 au total pour l'étape (de 13h20 à 15h05), dont :
- **JAVA_HOME** pointait sur Java 17 : j'ai dû le régler sur le JDK 21 au niveau utilisateur Windows pour lancer mvnw (environ 5 min) ; Java 21 obligatoire ajouté au README ;
- Windows (contrôle intelligent des applications) bloquait le module natif de Vite 8 : passage temporaire à Vite 6, puis retour à Vite 8 une fois le blocage désactivé (environ 15 min) ;
- start.spring.io ne propose plus Spring Boot 3 : pom.xml écrit à la main avec Spring Boot 3.5.16, wrapper Maven récupéré d'un projet généré (environ 5 min) ;
- le test d'intégration nommé **PresenceControllerIT** n'était pas lancé par **./mvnw test** (Maven ne lance que les classes en Test) : renommé **PresenceControllerIntegrationTest** ;
- une note **12.5** était acceptée et tronquée en 12 sans erreur par Jackson : la note est désormais lue brute pour renvoyer **400 NOTE_INVALIDE** (RG3) ;
- quelques coupures réseau vers GitHub pendant les fusions, vérifiées ensuite ;
- docker compose écrit mais non testé : le démon Docker est arrêté sur le poste.

**IA :** elle a écrit le code issue par issue selon un plan que j'ai validé, avec deux pauses pour que je teste moi-même : pause 1 après le socle (backend démarré, trois onglets, **404 RESSOURCE_INTROUVABLE** au format imposé) et pause 2 après #3 (quatre requêtes Bruno 201, 409, 410, 400), menée en parallèle de la suite du développement. Vérification : **./mvnw clean verify** (34 tests) et **npm run build** avant chaque fusion ; le test unitaire RG1 a été vérifié en cassant volontairement la règle (il échoue alors, puis la règle est rétablie) ; collection Bruno exécutée en ligne de commande (5 requêtes sur 5, 9 assertions sur 9) ; parcours complet dans le navigateur sur des ports de test : session, présences, dépôt, relecture refusée à 12.5 puis acceptée à 14, tableau passé à 14/20.

---

## Étape 3 — Enveloppe

**Retour de la pause 2 (fin de l'étape 2) :** tests Bruno validés : **201**, **409 DEJA_PRESENT**, **410 CODE_EXPIRE**, **400 CODE_INCONNU**. Un 409 inattendu venait d'un double envoi de ma part, confirmé par la Timeline de Bruno.

**Fait :**
- **Bug** (issue #26, créée avant tout code) : deux étudiants qui saisissent le code en même temps. Cause : la présence et l'attribution d'un relecteur aux exercices en attente partagent la même transaction ; deux présences simultanées créaient la même relecture, la contrainte **uk_relecture_exercice** annulait la seconde transaction, **présence comprise**, avec une erreur 500. Test concurrent poussé seul et rouge (**[201, 500, 500, 500]**, double envoi **[201, 500]**), puis correctif « Closes #26 » : verrou d'écriture sur la session (SELECT … FOR UPDATE). Test vert, relancé trois fois (PR #27).
- **Changement de besoin** (issues #28 et #29) : deux relecteurs par exercice, note = moyenne des deux, provisoire si une seule est rendue. Analyse mise à jour avant le code, dans des commits qui le disent (CDC v2 : RG8 remplacée, RG22, RG23, EF15, section 7 ; diagrammes D1 à D4 v2) ; migration **V3** (nouvelle contrainte UNIQUE (exercice_id, relecteur_id), V1 et V2 intactes, données conservées) ; contrat : champ **moyenneProvisoire** ajouté au tableau. Deux branches, deux PR (#30, #31), séparées du correctif.

**Bloqué :** environ 50 min au total pour l'étape (de 15h20 à 16h10), dont :
- le test du bug devait être fiable : je l'ai fait tourner sur 5 tours de 4 présences simultanées avec 3 exercices en attente, pour qu'il échoue à coup sûr avant correction ;
- la suppression de l'ancienne contrainte unique : la nouvelle est ajoutée avant, sinon la clé étrangère sur exercice_id perdait son index ;
- mon test « V1, V2, V3 dans l'ordre » échouait à cause de la ligne sans version que Flyway écrit pour la création du schéma (filtrée) ;
- deux nouvelles coupures réseau vers GitHub, rattrapées.

**Ce que j'ai sorti du périmètre pour absorber le changement, et pourquoi :** toutes les EF **Should** et **Could** (EF9 à EF14, issues #9 à #14) sont **reportées après v1.0** et étiquetées « Reporté » sur GitHub. Le bug et le changement du client sont Must, et le temps restant sert à livrer la v1.0 et la soumission avant 18h. Le sacrifice le plus coûteux est la présence ajoutée à la main (#9) : il faut désormais trois présents pour qu'un exercice ait ses deux relecteurs ; un exercice incomplet reste « en attente » au tableau. Viennent ensuite la clôture (#10), sans laquelle une session n'est jamais figée, puis le confort (#11 à #14). Ordre de reprise s'il reste du temps : #9, puis #10.

**IA :** elle a diagnostiqué la cause du bug en lisant le code, puis proposé le plan (issue, test rouge seul, correctif, évolution sur deux branches), que j'ai validé. J'ai tranché deux décisions : pas d'effet rétroactif (une note déjà rendue avec un seul relecteur reste définitive) et le report des Should/Could. Vérification : le test concurrent échoue avant le correctif et passe après (trois exécutions) ; **MigrationV3Test** applique V1, V2 puis V3 et retrouve les 4 relectures de démonstration (notes 15 et 12) ; 41 tests verts avec **./mvnw clean verify** ; contrat validé par le validateur OpenAPI ; diagrammes validés par l'analyseur Mermaid ; parcours réel sur des ports de test : après la note de Boris (14), tableau « 14.5/20 (provisoire) », après celle de Carine (17), « 15.25/20 » définitive.

---

## Étape 4 — Version finale

**Fait :** **CHANGELOG.md** avec les sections v1.0 et v0.1 (Corrigé, Modifié, Ajouté, Reporté), chaque ligne citant son issue et sa PR (issue #33, PR #36) ; README final : prérequis, démarrage en trois commandes, justification de React, données de démonstration (qui est qui), tests, écarts et fonctionnalités reportées (issue #34, PR #35) ; backlog trié : les six issues ouvertes (#9 à #14) ont leur priorité (**Should** ou **Could**) et l'étiquette **Reporté** ; jalon **[JALON] v1.0** posé juste après cette entrée.

**Bloqué :** environ 20 min au total pour l'étape (de 16h10 à 16h30), dont :
- la version de Node demandée pour le README (18 et plus) était trop basse : Vite 8 exige **Node.js 20.19** ou plus récent (ou 22.12 et plus), vérifié dans ses dépendances ; c'est la version écrite dans le README ;
- le test depuis un clone vierge devait éviter le port 8080, occupé par mon propre backend : clone dans un dossier temporaire, backend sur 18080 et frontend sur 5174, puis dossier supprimé.

**IA :** elle a rédigé le CHANGELOG à partir de la liste des PR fusionnées (gh pr list) et le README à partir du code et des données de démonstration. Vérification : chaque numéro d'issue et de PR du CHANGELOG comparé à GitHub, et le numéro de la PR du CHANGELOG contrôlé à sa création ; README suivi à la lettre depuis un clone vierge : Flyway applique V1, V2 puis V3, les trois onglets s'affichent, le tableau correspond exactement à la table « qui est qui » du README, et **./mvnw test** passe dans le clone (41 tests).

## Après v1.0 — Refonte de l'interface (hors barème)

**Fait :** issue #38 (**Could**), branche **style/38-interface**, huit commits : variables CSS et composants réutilisables (Carte, Champ, Badge, messages de succès et état vide), en-tête avec sous-titre, barre **Promotion** / **Je suis**, onglets avec aide et onglet actif en évidence, écran Formateur (code de session en très grand avec son heure d'expiration, tableau à en-tête contrasté, lignes alternées, badge **provisoire**, tiret gris sans note, badge des relectures à faire), écran Étudiant en deux cartes (code en majuscules et en grande police), écran Relecteur en cartes avec état vide « Aucune relecture à faire », responsive jusqu'à 360 px. Retouche d'affichage : les écrans Étudiant et Relecteur repartent à zéro quand on change de nom (les messages de l'étudiant précédent restaient affichés). Aucun changement dans le backend, le contrat, le dossier frontend/src/api ni les dépendances.

**Bloqué :** environ 10 min sur les 35 de l'étape (de 18h05 à 18h40). Le navigateur de test refusait par moments certaines requêtes vers le poste (**ERR_NETWORK_ACCESS_DENIED**, puis **502** du proxy sur un **EACCES** vers le backend) : ce sont des blocages du poste, pas de l'application, qui les affiche d'ailleurs correctement en erreur **RESEAU**. Contournement : frontend compilé servi par vite preview sur 127.0.0.1 (deux fichiers à charger au lieu de dizaines de modules), et action relancée après chaque blocage.

**IA :** elle a écrit le CSS et découpé les composants. Vérification : **npm run build** et **npm run lint** après chaque commit ; diff vide sur backend, api, frontend/src/api et package.json ; **./mvnw test** toujours à 41 tests verts ; parcours complet dans le navigateur sur une base H2 séparée (backend 18080, frontend 5174) : ouverture de session, **410 CODE_EXPIRE** avec DEMO01, présence, **409 DEJA_PRESENT**, dépôt, **400 NOTE_INVALIDE** avec une note de 25, première relecture puis moyenne **14.5/20 provisoire**, seconde relecture puis **15/20** définitive, état vide du relecteur ; à 360 px, la page ne défile pas horizontalement et seul le tableau défile, la colonne des noms restant visible.
