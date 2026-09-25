import { useState } from 'react'
import type { FormEvent } from 'react'
import { ApiError } from '../api/client'
import { marquerPresence } from '../api/presences'
import type { Etudiant } from '../api/types'
import { Carte } from '../composants/Carte'
import { Champ } from '../composants/Champ'
import { MessageErreur, MessageSucces } from '../composants/Etat'

/** EF3 : l'étudiant saisit le code donné par le formateur (utilisable sur téléphone, ENF1). */
export function MarquerPresence({ etudiant }: { etudiant: Etudiant }) {
  const [code, setCode] = useState('')
  const [envoi, setEnvoi] = useState(false)
  const [erreur, setErreur] = useState<ApiError>()
  const [succes, setSucces] = useState(false)

  async function envoyer(e: FormEvent) {
    e.preventDefault()
    setEnvoi(true)
    setErreur(undefined)
    setSucces(false)
    try {
      await marquerPresence(code, etudiant.id)
      setSucces(true)
      setCode('')
    } catch (err) {
      setErreur(err as ApiError)
    } finally {
      setEnvoi(false)
    }
  }

  return (
    <Carte titre="Marquer ma présence" aide="Saisissez le code de 6 caractères affiché par le formateur.">
      <form className="formulaire" onSubmit={envoyer}>
        <Champ libelle="Code de présence">
          <input
            className="saisie-code"
            value={code}
            onChange={(e) => setCode(e.target.value.toUpperCase())}
            placeholder="ABC234"
            autoCapitalize="characters"
            autoComplete="off"
            spellCheck={false}
            maxLength={6}
            required
          />
        </Champ>
        <button type="submit" className="bouton-principal" disabled={envoi}>
          {envoi ? 'Envoi…' : 'Je suis présent'}
        </button>
        <MessageErreur erreur={erreur} />
        {succes && <MessageSucces>Présence enregistrée.</MessageSucces>}
      </form>
    </Carte>
  )
}
