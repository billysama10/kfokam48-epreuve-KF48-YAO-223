-- V3 : changement de besoin de l'étape 3, deux relecteurs par exercice (#28).
-- RG8 : « un seul relecteur » (Q6) devient « deux relecteurs différents ».
-- RG22 : un même étudiant ne relit pas deux fois le même exercice.
-- Aucune donnée n'est modifiée ni supprimée ; V1 et V2 restent intactes.

-- La nouvelle contrainte est ajoutée d'abord : la clé étrangère sur exercice_id garde un index.
ALTER TABLE relecture ADD CONSTRAINT uk_relecture_exercice_relecteur UNIQUE (exercice_id, relecteur_id);

ALTER TABLE relecture DROP CONSTRAINT uk_relecture_exercice;
