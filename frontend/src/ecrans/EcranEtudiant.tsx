import type { Etudiant } from '../api/types'
import { DeposerExercice } from './DeposerExercice'
import { MarquerPresence } from './MarquerPresence'

/** Écran étudiant (F2) : marquer sa présence, puis déposer son exercice. */
export function EcranEtudiant({ etudiant }: { etudiant: Etudiant }) {
  return (
    <div className="pile">
      <div className="salutation">
        <h2>Bonjour {etudiant.nom}</h2>
        <p className="carte-aide">Marquez d'abord votre présence avec le code du formateur, puis déposez votre exercice.</p>
      </div>
      <div className="grille-deux">
        <MarquerPresence etudiant={etudiant} />
        <DeposerExercice etudiant={etudiant} />
      </div>
    </div>
  )
}
