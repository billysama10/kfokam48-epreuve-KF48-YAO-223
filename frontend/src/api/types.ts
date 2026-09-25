// Types des réponses de l'API, alignés sur api/contrat.yaml.

export type Promotion = { id: number; nom: string }

export type Etudiant = { id: number; nom: string; promotionId: number }

export type SessionOuverte = { id: number; code: string; ouvertureAt: string; expirationAt: string }
