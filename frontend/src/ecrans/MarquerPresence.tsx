import { useState } from 'react'
import type { FormEvent } from 'react'
import { ApiError } from '../api/client'
import { marquerPresence } from '../api/presences'
import type { Etudiant } from '../api/types'
import { MessageErreur } from '../composants/Etat'

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
    <div>
      <h2>Marquer ma présence</h2>
      <form onSubmit={envoyer}>
        <input
          value={code}
          onChange={(e) => setCode(e.target.value.toUpperCase())}
          placeholder="Code de présence"
          aria-label="Code de présence"
          autoCapitalize="characters"
          autoComplete="off"
          maxLength={6}
          required
        />{' '}
        <button type="submit" disabled={envoi}>
          {envoi ? 'Envoi…' : 'Je suis présent'}
        </button>
      </form>
      <MessageErreur erreur={erreur} />
      {succes && <p className="succes">Présence enregistrée.</p>}
    </div>
  )
}
