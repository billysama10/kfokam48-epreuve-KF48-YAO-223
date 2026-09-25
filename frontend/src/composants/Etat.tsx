import type { ApiError } from '../api/client'

/** Affichage commun des états de chargement et d'erreur (F3, ENF6). */
export function Chargement() {
  return <p className="chargement">Chargement…</p>
}

export function MessageErreur({ erreur }: { erreur: ApiError | undefined }) {
  if (!erreur) return null
  return (
    <p className="erreur" role="alert">
      {erreur.message} <small>({erreur.code})</small>
    </p>
  )
}
