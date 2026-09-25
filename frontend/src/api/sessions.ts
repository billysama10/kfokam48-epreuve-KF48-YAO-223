import { appeler } from './client'
import type { SessionOuverte } from './types'

/** POST /api/sessions (opération imposée, EF2). */
export function ouvrirSession(titre: string, promotionId: number): Promise<SessionOuverte> {
  return appeler('/api/sessions', { methode: 'POST', corps: { titre, promotionId } })
}
