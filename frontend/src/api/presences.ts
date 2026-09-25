import { appeler } from './client'
import type { Presence } from './types'

/** POST /api/presences (opération imposée, EF3). */
export function marquerPresence(code: string, etudiantId: number): Promise<Presence> {
  return appeler('/api/presences', { methode: 'POST', corps: { code, etudiantId } })
}
