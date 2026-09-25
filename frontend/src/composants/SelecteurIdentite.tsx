import { useEffect } from 'react'
import { listerEtudiants, listerPromotions } from '../api/promotions'
import type { Etudiant, Promotion } from '../api/types'
import { useAppel } from '../api/useAppel'
import { Chargement, MessageErreur } from './Etat'

type Props = {
  promotion: Promotion | undefined
  etudiant: Etudiant | undefined
  onPromotion: (p: Promotion | undefined) => void
  onEtudiant: (e: Etudiant | undefined) => void
}

/** EF1, Q1 : pas de mot de passe, on choisit son nom dans la liste de sa promotion. */
export function SelecteurIdentite({ promotion, etudiant, onPromotion, onEtudiant }: Props) {
  const promotions = useAppel(listerPromotions, [])
  const etudiants = useAppel(
    () => (promotion ? listerEtudiants(promotion.id) : Promise.resolve([])),
    [promotion?.id],
  )

  // Une seule promotion en démonstration : on la présélectionne.
  useEffect(() => {
    if (!promotion && promotions.donnees?.length === 1) onPromotion(promotions.donnees[0])
  }, [promotion, promotions.donnees, onPromotion])

  if (promotions.chargement) return <Chargement />

  return (
    <div className="identite">
      <MessageErreur erreur={promotions.erreur ?? etudiants.erreur} />
      <label>
        Promotion{' '}
        <select
          value={promotion?.id ?? ''}
          onChange={(e) => {
            onPromotion(promotions.donnees?.find((p) => p.id === Number(e.target.value)))
            onEtudiant(undefined)
          }}
        >
          <option value="">— choisir —</option>
          {promotions.donnees?.map((p) => (
            <option key={p.id} value={p.id}>
              {p.nom}
            </option>
          ))}
        </select>
      </label>{' '}
      <label>
        Je suis{' '}
        <select
          value={etudiant?.id ?? ''}
          disabled={!promotion || etudiants.chargement}
          onChange={(e) => onEtudiant(etudiants.donnees?.find((x) => x.id === Number(e.target.value)))}
        >
          <option value="">— choisir mon nom —</option>
          {etudiants.donnees?.map((x) => (
            <option key={x.id} value={x.id}>
              {x.nom}
            </option>
          ))}
        </select>
      </label>
    </div>
  )
}
