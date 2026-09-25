import { useState } from 'react'
import type { FormEvent } from 'react'
import { ApiError } from '../api/client'
import { deposerExercice } from '../api/exercices'
import { listerSessions } from '../api/sessions'
import type { Etudiant, Exercice } from '../api/types'
import { useAppel } from '../api/useAppel'
import { Chargement, MessageErreur } from '../composants/Etat'

const LIBELLE_STATUT: Record<string, string> = {
  OUVERTE: 'code valide',
  EXPIREE: 'code expiré, dépôt possible',
  CLOTUREE: 'clôturée',
}

/** EF4 : dépôt du lien, possible jusqu'à la clôture, même si le code a expiré (RG13). */
export function DeposerExercice({ etudiant }: { etudiant: Etudiant }) {
  const sessions = useAppel(() => listerSessions(etudiant.promotionId), [etudiant.promotionId])
  const [sessionId, setSessionId] = useState('')
  const [lien, setLien] = useState('')
  const [envoi, setEnvoi] = useState(false)
  const [erreur, setErreur] = useState<ApiError>()
  const [depose, setDepose] = useState<Exercice>()

  async function envoyer(e: FormEvent) {
    e.preventDefault()
    setEnvoi(true)
    setErreur(undefined)
    setDepose(undefined)
    try {
      setDepose(await deposerExercice(Number(sessionId), etudiant.id, lien))
      setLien('')
    } catch (err) {
      setErreur(err as ApiError)
    } finally {
      setEnvoi(false)
    }
  }

  return (
    <div>
      <h2>Déposer mon exercice</h2>
      {sessions.chargement && <Chargement />}
      <MessageErreur erreur={sessions.erreur} />
      {sessions.donnees && (
        <form onSubmit={envoyer}>
          <select value={sessionId} onChange={(e) => setSessionId(e.target.value)} aria-label="Session" required>
            <option value="">— choisir la session —</option>
            {sessions.donnees.map((s) => (
              <option key={s.id} value={s.id} disabled={s.statut === 'CLOTUREE'}>
                {s.titre} ({LIBELLE_STATUT[s.statut]})
              </option>
            ))}
          </select>{' '}
          <input
            type="url"
            value={lien}
            onChange={(e) => setLien(e.target.value)}
            placeholder="https://github.com/…"
            aria-label="Lien de l'exercice"
            required
          />{' '}
          <button type="submit" disabled={envoi}>
            {envoi ? 'Envoi…' : 'Déposer'}
          </button>
        </form>
      )}
      <MessageErreur erreur={erreur} />
      {depose && <p className="succes">Exercice déposé (statut : {depose.statut}).</p>}
    </div>
  )
}
