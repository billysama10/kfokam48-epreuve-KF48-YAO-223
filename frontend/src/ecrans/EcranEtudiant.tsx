import type { Etudiant } from '../api/types'
import { DeposerExercice } from './DeposerExercice'
import { MarquerPresence } from './MarquerPresence'

/** Écran étudiant (F2) : marquer sa présence, puis déposer son exercice. */
export function EcranEtudiant({ etudiant }: { etudiant: Etudiant }) {
  return (
    <div>
      <p>Bonjour {etudiant.nom}.</p>
      <MarquerPresence etudiant={etudiant} />
      <DeposerExercice etudiant={etudiant} />
    </div>
  )
}
