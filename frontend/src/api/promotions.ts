import { appeler } from './client'
import type { Etudiant, Promotion } from './types'

export function listerPromotions(): Promise<Promotion[]> {
  return appeler('/api/promotions')
}

export function listerEtudiants(promotionId: number): Promise<Etudiant[]> {
  return appeler(`/api/promotions/${promotionId}/etudiants`)
}
