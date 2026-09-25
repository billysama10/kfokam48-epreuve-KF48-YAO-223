import { useState } from 'react'
import type { FormEvent } from 'react'
import { ApiError } from '../api/client'
import { deposerExercice } from '../api/exercices'
import { listerSessions } from '../api/sessions'
import type { Etudiant, Exercice } from '../api/types'
import { useAppel } from '../api/useAppel'
import { Carte } from '../composants/Carte'
import { Champ } from '../composants/Champ'
import { Chargement, EtatVide, MessageErreur, MessageSucces } from '../composants/Etat'

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
    <Carte titre="Déposer mon exercice" aide="Collez le lien de votre travail (dépôt GitHub, par exemple).">
      <div className="pile">
        {sessions.chargement && <Chargement />}
        <MessageErreur erreur={sessions.erreur} />
        {sessions.donnees?.length === 0 && <EtatVide>Aucune session pour votre promotion pour le moment.</EtatVide>}
        {sessions.donnees && sessions.donnees.length > 0 && (
          <form className="formulaire" onSubmit={envoyer}>
            <Champ libelle="Session" aide="Le dépôt reste possible après l'expiration du code.">
              <select value={sessionId} onChange={(e) => setSessionId(e.target.value)} required>
                <option value="">— choisir la session —</option>
                {sessions.donnees.map((s) => (
                  <option key={s.id} value={s.id} disabled={s.statut === 'CLOTUREE'}>
                    {s.titre} ({LIBELLE_STATUT[s.statut]})
                  </option>
                ))}
              </select>
            </Champ>
            <Champ libelle="Lien de l'exercice">
              <input
                type="url"
                value={lien}
                onChange={(e) => setLien(e.target.value)}
                placeholder="https://github.com/…"
                required
              />
            </Champ>
            <button type="submit" className="bouton-principal" disabled={envoi}>
              {envoi ? 'Envoi…' : 'Déposer'}
            </button>
          </form>
        )}
        <MessageErreur erreur={erreur} />
        {depose && <MessageSucces>Exercice déposé (statut : {depose.statut}).</MessageSucces>}
      </div>
    </Carte>
  )
}
