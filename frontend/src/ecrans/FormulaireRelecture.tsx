import { useState } from 'react'
import type { FormEvent } from 'react'
import { ApiError } from '../api/client'
import { rendreRelecture } from '../api/relectures'
import type { RelectureAttribuee } from '../api/types'
import { Champ } from '../composants/Champ'
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
    <form className="formulaire" onSubmit={envoyer}>
      <Champ libelle="Note sur 20" aide="Nombre entier de 0 à 20.">
        <input
          className="saisie-note"
          type="number"
          inputMode="numeric"
          value={note}
          onChange={(e) => setNote(e.target.value)}
          placeholder="0 à 20"
          required
        />
      </Champ>
      <Champ libelle="Commentaire" aide="Ce qui est réussi, ce qui peut être amélioré.">
        <textarea value={commentaire} onChange={(e) => setCommentaire(e.target.value)} rows={4} required />
      </Champ>
      <div className="actions">
        <button type="submit" className="bouton-principal" disabled={envoi}>
          {envoi ? 'Envoi…' : 'Rendre ma relecture (définitif)'}
        </button>
        <span className="champ-aide">Une relecture rendue ne peut plus être modifiée.</span>
      </div>
      <MessageErreur erreur={erreur} />
    </form>
  )
}
