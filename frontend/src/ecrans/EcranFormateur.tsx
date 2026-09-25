import { useState } from 'react'
import type { FormEvent } from 'react'
import { ApiError } from '../api/client'
import { ouvrirSession } from '../api/sessions'
import type { Promotion, SessionOuverte } from '../api/types'
import { Carte } from '../composants/Carte'
import { Champ } from '../composants/Champ'
import { CodeSession } from '../composants/CodeSession'
import { EtatVide, MessageErreur } from '../composants/Etat'
import { Tableau } from './Tableau'

/** Écran formateur (F2) : ouvrir une session (EF2) et voir le tableau de la promotion (EF8). */
export function EcranFormateur({ promotion }: { promotion: Promotion | undefined }) {
  const [titre, setTitre] = useState('')
  const [envoi, setEnvoi] = useState(false)
  const [erreur, setErreur] = useState<ApiError>()
  const [session, setSession] = useState<SessionOuverte>()

  if (!promotion) return <EtatVide>Choisissez d'abord la promotion ci-dessus.</EtatVide>

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
    <div className="pile">
      <Carte titre="Ouvrir une session" aide="Un code de présence est créé : affichez-le pour que les étudiants le saisissent.">
        <div className="pile">
          <form className="formulaire" onSubmit={ouvrir}>
            <Champ libelle="Titre du cours">
              <input
                value={titre}
                onChange={(e) => setTitre(e.target.value)}
                placeholder="Par exemple : Introduction à Spring Boot"
                required
              />
            </Champ>
            <button type="submit" className="bouton-principal" disabled={envoi}>
              {envoi ? 'Ouverture…' : 'Ouvrir la session'}
            </button>
          </form>
          <MessageErreur erreur={erreur} />
          {session && <CodeSession session={session} />}
        </div>
      </Carte>
      <Tableau key={session?.id ?? 0} promotion={promotion} />
    </div>
  )
}
