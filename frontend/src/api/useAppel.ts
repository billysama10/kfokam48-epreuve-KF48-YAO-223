import { useCallback, useEffect, useState } from 'react'
import { ApiError } from './client'

/** État commun d'un appel : chargement, erreur, données (F3). */
export type EtatAppel<T> = {
  donnees: T | undefined
  chargement: boolean
  erreur: ApiError | undefined
  recharger: () => void
}

/** Lance l'appel au montage et à chaque changement de dépendances. */
export function useAppel<T>(appel: () => Promise<T>, dependances: unknown[]): EtatAppel<T> {
  const [donnees, setDonnees] = useState<T>()
  const [chargement, setChargement] = useState(true)
  const [erreur, setErreur] = useState<ApiError>()
  const [compteur, setCompteur] = useState(0)

  const appelMemo = useCallback(appel, dependances)

  useEffect(() => {
    let actif = true
    setChargement(true)
    setErreur(undefined)
    appelMemo()
      .then((d) => actif && setDonnees(d))
      .catch((e: unknown) => actif && setErreur(e instanceof ApiError ? e : new ApiError(0, 'ERREUR_INCONNUE', String(e))))
      .finally(() => actif && setChargement(false))
    return () => {
      actif = false
    }
  }, [appelMemo, compteur])

  return { donnees, chargement, erreur, recharger: () => setCompteur((c) => c + 1) }
}
