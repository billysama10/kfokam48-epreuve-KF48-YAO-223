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
