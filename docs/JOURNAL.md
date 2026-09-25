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
