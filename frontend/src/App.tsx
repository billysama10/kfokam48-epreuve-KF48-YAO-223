import { useState } from 'react'
import type { Etudiant, Promotion } from './api/types'
import { Entete } from './composants/Entete'
import { EtatVide } from './composants/Etat'
import { Onglets } from './composants/Onglets'
import type { OngletDef } from './composants/Onglets'
import { SelecteurIdentite } from './composants/SelecteurIdentite'
import { EcranEtudiant } from './ecrans/EcranEtudiant'
import { EcranFormateur } from './ecrans/EcranFormateur'
import { EcranRelecteur } from './ecrans/EcranRelecteur'

type Onglet = 'formateur' | 'etudiant' | 'relecteur'

const ONGLETS: OngletDef<Onglet>[] = [
  { id: 'formateur', libelle: 'Formateur', aide: 'Ouvrir une session et suivre la promotion' },
  { id: 'etudiant', libelle: 'Étudiant', aide: 'Marquer ma présence et déposer mon exercice' },
  { id: 'relecteur', libelle: 'Relecteur', aide: 'Noter les exercices qui me sont attribués' },
]

function App() {
  const [onglet, setOnglet] = useState<Onglet>('formateur')
  const [promotion, setPromotion] = useState<Promotion>()
  const [etudiant, setEtudiant] = useState<Etudiant>()

  return (
    <div className="page pile">
      <Entete />
      <SelecteurIdentite
        promotion={promotion}
        etudiant={etudiant}
        onPromotion={setPromotion}
        onEtudiant={setEtudiant}
      />
      <Onglets onglets={ONGLETS} actif={onglet} onChange={setOnglet} />
      {/* key : un changement de nom repart d'un écran vierge, sans les messages du précédent */}
      <main id="panneau" role="tabpanel" aria-labelledby={`onglet-${onglet}`}>
        {onglet === 'formateur' && <EcranFormateur promotion={promotion} />}
        {onglet !== 'formateur' && !etudiant && (
          <EtatVide>Choisissez d'abord votre nom dans la liste « Je suis » ci-dessus.</EtatVide>
        )}
        {onglet === 'etudiant' && etudiant && <EcranEtudiant key={etudiant.id} etudiant={etudiant} />}
        {onglet === 'relecteur' && etudiant && <EcranRelecteur key={etudiant.id} etudiant={etudiant} />}
      </main>
    </div>
  )
}

export default App
