import { appeler } from './client'
import type { LigneTableau } from './types'

/** GET /api/tableau?promotionId= (opération imposée, EF8). */
export function chargerTableau(promotionId: number): Promise<LigneTableau[]> {
  return appeler(`/api/tableau?promotionId=${promotionId}`)
}
