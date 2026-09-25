import type { ReactNode } from 'react'
import type { ApiError } from '../api/client'

/** Affichage commun des états de chargement, d'erreur, de succès et de liste vide (F3, ENF6). */
export function Chargement() {
  return (
    <p className="chargement" role="status">
      Chargement…
    </p>
  )
}

/** Erreur de l'API en rouge, avec son code (par exemple 410 CODE_EXPIRE). */
export function MessageErreur({ erreur }: { erreur: ApiError | undefined }) {
  if (!erreur) return null
  return (
    <div className="message message-erreur" role="alert">
      <span>{erreur.message}</span>
      <code className="message-code">
        {erreur.statut > 0 ? `${erreur.statut} ` : ''}
        {erreur.code}
      </code>
    </div>
  )
}

export function MessageSucces({ children }: { children: ReactNode }) {
  return (
    <div className="message message-succes" role="status">
      {children}
    </div>
  )
}

export function EtatVide({ children }: { children: ReactNode }) {
  return <p className="etat-vide">{children}</p>
}
