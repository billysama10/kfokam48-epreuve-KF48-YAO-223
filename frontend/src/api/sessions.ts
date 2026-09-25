import { appeler } from './client'
import type { SessionOuverte, SessionResume } from './types'

/** POST /api/sessions (opération imposée, EF2). */
export function ouvrirSession(titre: string, promotionId: number): Promise<SessionOuverte> {
  return appeler('/api/sessions', { methode: 'POST', corps: { titre, promotionId } })
}

/** GET /api/promotions/{id}/sessions, la plus récente en premier (EF4). */
export function listerSessions(promotionId: number): Promise<SessionResume[]> {
  return appeler(`/api/promotions/${promotionId}/sessions`)
}
