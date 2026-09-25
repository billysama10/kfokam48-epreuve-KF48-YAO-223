import { useState } from 'react'
import type { FormEvent } from 'react'
import { ApiError } from '../api/client'
import { ouvrirSession } from '../api/sessions'
import type { Promotion, SessionOuverte } from '../api/types'
import { MessageErreur } from '../composants/Etat'
import { heure } from '../composants/Heure'

/** Écran formateur (F2) : ouvrir une session et obtenir le code de présence (EF2). */
export function EcranFormateur({ promotion }: { promotion: Promotion | undefined }) {
  const [titre, setTitre] = useState('')
  const [envoi, setEnvoi] = useState(false)
  const [erreur, setErreur] = useState<ApiError>()
  const [session, setSession] = useState<SessionOuverte>()

  if (!promotion) return <p>Choisissez d'abord la promotion ci-dessus.</p>

  async function ouvrir(e: FormEvent) {
    e.preventDefault()
    if (!promotion) return
    setEnvoi(true)
    setErreur(undefined)
    try {
      setSession(await ouvrirSession(titre, promotion.id))
      setTitre('')
    } catch (err) {
      setErreur(err as ApiError)
    } finally {
      setEnvoi(false)
    }
  }

  return (
    <div>
      <h2>Ouvrir une session</h2>
      <form onSubmit={ouvrir}>
        <input value={titre} onChange={(e) => setTitre(e.target.value)} placeholder="Titre du cours" required />{' '}
        <button type="submit" disabled={envoi}>
          {envoi ? 'Ouverture…' : 'Ouvrir la session'}
        </button>
      </form>
      <MessageErreur erreur={erreur} />
      {session && (
        <div className="succes">
          <p>
            Code de présence : <strong className="code">{session.code}</strong>
          </p>
          <p>
            Valable jusqu'à {heure(session.expirationAt)} (ouverte à {heure(session.ouvertureAt)}).
          </p>
        </div>
      )}
    </div>
  )
}
