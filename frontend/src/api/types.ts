// Types des réponses de l'API, alignés sur api/contrat.yaml.

export type Promotion = { id: number; nom: string }

export type Etudiant = { id: number; nom: string; promotionId: number }

export type SessionOuverte = { id: number; code: string; ouvertureAt: string; expirationAt: string }

export type StatutSession = 'OUVERTE' | 'EXPIREE' | 'CLOTUREE'

export type SessionResume = {
  id: number
  titre: string
  promotionId: number
  code: string
  ouvertureAt: string
  expirationAt: string
  clotureeAt: string | null
  statut: StatutSession
}

export type StatutExercice = 'DEPOSE' | 'EN_ATTENTE_RELECTURE' | 'RELU'

export type Exercice = { id: number; statut: StatutExercice }

export type Presence ={ id: number; sessionId: number; etudiantId: number; source: 'ETUDIANT' | 'FORMATEUR' }
