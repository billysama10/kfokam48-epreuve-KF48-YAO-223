import { appeler } from './client'
import type { RelectureAttribuee } from './types'

/** GET /api/etudiants/{id}/relectures (EF6). */
export function listerRelectures(etudiantId: number): Promise<RelectureAttribuee[]> {
  return appeler(`/api/etudiants/${etudiantId}/relectures`)
}

/**
 * POST /api/relectures/{id} (opération imposée, EF7). L'en-tête X-Etudiant-Id identifie
 * le relecteur, pour que le serveur applique RG2 et RG19.
 */
export function rendreRelecture(
  relectureId: number,
  relecteurId: number,
  note: number,
  commentaire: string,
): Promise<RelectureAttribuee> {
  return appeler(`/api/relectures/${relectureId}`, {
    methode: 'POST',
    corps: { note, commentaire },
    entetes: { 'X-Etudiant-Id': String(relecteurId) },
  })
}
