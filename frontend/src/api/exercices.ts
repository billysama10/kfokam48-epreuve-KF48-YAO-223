import { appeler } from './client'
import type { Exercice } from './types'

/** POST /api/exercices (opération imposée, EF4). */
export function deposerExercice(sessionId: number, etudiantId: number, lien: string): Promise<Exercice> {
  return appeler('/api/exercices', { methode: 'POST', corps: { sessionId, etudiantId, lien } })
}
