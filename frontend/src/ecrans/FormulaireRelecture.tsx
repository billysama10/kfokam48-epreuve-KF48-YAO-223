import { useState } from 'react'
import type { FormEvent } from 'react'
import { ApiError } from '../api/client'
import { rendreRelecture } from '../api/relectures'
import type { RelectureAttribuee } from '../api/types'
import { MessageErreur } from '../composants/Etat'

type Props = {
  relecture: RelectureAttribuee
  relecteurId: number
  onRendue: () => void
}

/**
 * EF7 : note et commentaire. Le champ n'empêche pas une saisie invalide :
 * c'est l'API qui applique RG3 et renvoie NOTE_INVALIDE, affiché tel quel.
 */
export function FormulaireRelecture({ relecture, relecteurId, onRendue }: Props) {
  const [note, setNote] = useState('')
  const [commentaire, setCommentaire] = useState('')
  const [envoi, setEnvoi] = useState(false)
  const [erreur, setErreur] = useState<ApiError>()

  async function envoyer(e: FormEvent) {
    e.preventDefault()
    setEnvoi(true)
    setErreur(undefined)
    try {
      await rendreRelecture(relecture.id, relecteurId, Number(note), commentaire)
      onRendue()
    } catch (err) {
      setErreur(err as ApiError)
    } finally {
      setEnvoi(false)
    }
  }

  return (
    <form onSubmit={envoyer}>
      <label>
        Note sur 20{' '}
        <input type="number" value={note} onChange={(e) => setNote(e.target.value)} required />
      </label>
      <br />
      <label>
        Commentaire
        <br />
        <textarea value={commentaire} onChange={(e) => setCommentaire(e.target.value)} rows={3} cols={40} required />
      </label>
      <br />
      <button type="submit" disabled={envoi}>
        {envoi ? 'Envoi…' : 'Rendre la relecture (définitif)'}
      </button>
      <MessageErreur erreur={erreur} />
    </form>
  )
}
