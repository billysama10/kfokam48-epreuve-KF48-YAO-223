import type { SessionOuverte } from '../api/types'
import { heure } from './Heure'

/** Encadré du code de présence, en très grand pour être recopié depuis le fond de la salle. */
export function CodeSession({ session }: { session: SessionOuverte }) {
  return (
    <div className="code-session" role="status">
      <span className="code-session-libelle">Code de présence à donner aux étudiants</span>
      <strong className="code-session-valeur">{session.code}</strong>
      <span className="code-session-expiration">
        Valable jusqu'à <strong>{heure(session.expirationAt)}</strong> · session ouverte à{' '}
        {heure(session.ouvertureAt)}
      </span>
    </div>
  )
}
