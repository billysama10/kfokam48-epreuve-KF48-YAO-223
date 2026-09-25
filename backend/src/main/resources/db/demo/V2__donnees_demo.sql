-- V2 : données de démonstration (dossier db/demo, non chargé par les tests)
-- Pas d'identifiants explicites : les séquences d'identité restent cohérentes.

INSERT INTO promotion (nom) VALUES ('KF48 Yaoundé 2026');

INSERT INTO etudiant (nom, promotion_id)
SELECT e.nom, p.id
FROM (VALUES ('Awa Ngono'), ('Boris Kamga'), ('Carine Mbarga'),
             ('David Fotso'), ('Esther Nana'), ('Franck Tchinda')) AS e(nom)
CROSS JOIN promotion p
WHERE p.nom = 'KF48 Yaoundé 2026';

-- Une session passée : code expiré, non clôturée (dépôts encore possibles, RG13)
INSERT INTO session_cours (titre, promotion_id, code, ouverture_at, expiration_at, cloturee_at)
SELECT 'Introduction à Spring Boot', p.id, 'DEMO01',
       TIMESTAMP '2026-09-24 09:00:00', TIMESTAMP '2026-09-24 09:15:00', NULL
FROM promotion p WHERE p.nom = 'KF48 Yaoundé 2026';

-- Présences : quatre par code, une ajoutée par le formateur (Q14), Franck absent
INSERT INTO presence (session_id, etudiant_id, source, marquee_at)
SELECT s.id, e.id, v.source, TIMESTAMP '2026-09-24 09:05:00'
FROM (VALUES ('Awa Ngono', 'ETUDIANT'), ('Boris Kamga', 'ETUDIANT'), ('Carine Mbarga', 'ETUDIANT'),
             ('David Fotso', 'ETUDIANT'), ('Esther Nana', 'FORMATEUR')) AS v(nom, source)
JOIN etudiant e ON e.nom = v.nom
CROSS JOIN session_cours s
WHERE s.code = 'DEMO01';

-- Exercices : deux relus, deux en attente de relecture
INSERT INTO exercice (session_id, etudiant_id, lien, statut, depose_at, modifie_at)
SELECT s.id, e.id, v.lien, v.statut, TIMESTAMP '2026-09-24 18:00:00', NULL
FROM (VALUES ('Awa Ngono', 'https://github.com/awa-ngono/tp-spring', 'RELU'),
             ('Boris Kamga', 'https://github.com/boris-kamga/tp-spring', 'RELU'),
             ('Carine Mbarga', 'https://github.com/carine-mbarga/tp-spring', 'EN_ATTENTE_RELECTURE'),
             ('David Fotso', 'https://github.com/david-fotso/tp-spring', 'EN_ATTENTE_RELECTURE')) AS v(nom, lien, statut)
JOIN etudiant e ON e.nom = v.nom
CROSS JOIN session_cours s
WHERE s.code = 'DEMO01';

-- Relectures : relecteur toujours différent de l'auteur (RG2) et présent (RG9)
INSERT INTO relecture (exercice_id, relecteur_id, note, commentaire, attribuee_at, rendue_at)
SELECT x.id, r.id, v.note, v.commentaire, TIMESTAMP '2026-09-24 18:00:00', v.rendue_at
FROM (VALUES ('Awa Ngono', 'Boris Kamga', 15, 'Code propre, tests à compléter.', TIMESTAMP '2026-09-24 20:00:00'),
             ('Boris Kamga', 'Carine Mbarga', 12, 'Fonctionne, mais la validation manque.', TIMESTAMP '2026-09-24 21:00:00'),
             ('Carine Mbarga', 'Awa Ngono', NULL, NULL, NULL),
             ('David Fotso', 'Esther Nana', NULL, NULL, NULL)) AS v(auteur, relecteur, note, commentaire, rendue_at)
JOIN etudiant a ON a.nom = v.auteur
JOIN exercice x ON x.etudiant_id = a.id
JOIN etudiant r ON r.nom = v.relecteur;
