import { appeler } from './client'
import type { RelectureAttribuee } from './types'

/** GET /api/etudiants/{id}/relectures (EF6). */
export function listerRelectures(etudiantId: number): Promise<RelectureAttribuee[]> {
  return appeler(`/api/etudiants/${etudiantId}/relectures`)
}
