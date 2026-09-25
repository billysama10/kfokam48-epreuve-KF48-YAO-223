import { useState } from 'react'
import type { Etudiant, Promotion } from './api/types'
import { SelecteurIdentite } from './composants/SelecteurIdentite'
import { EcranEtudiant } from './ecrans/EcranEtudiant'
import { EcranFormateur } from './ecrans/EcranFormateur'

type Onglet = 'formateur' | 'etudiant' | 'relecteur'

const ONGLETS: { id: Onglet; libelle: string }[] = [
  { id: 'formateur', libelle: 'Formateur' },
  { id: 'etudiant', libelle: 'Étudiant' },
  { id: 'relecteur', libelle: 'Relecteur' },
]

function App() {
  const [onglet, setOnglet] = useState<Onglet>('formateur')
  const [promotion, setPromotion] = useState<Promotion>()
  const [etudiant, setEtudiant] = useState<Etudiant>()

  return (
    <main>
      <h1>KF48 Présences et Relectures</h1>
      <SelecteurIdentite
        promotion={promotion}
        etudiant={etudiant}
        onPromotion={setPromotion}
        onEtudiant={setEtudiant}
      />
      <nav>
        {ONGLETS.map((o) => (
          <button key={o.id} className={o.id === onglet ? 'actif' : ''} onClick={() => setOnglet(o.id)}>
            {o.libelle}
          </button>
        ))}
      </nav>
      <section>
        {onglet === 'formateur' && <EcranFormateur promotion={promotion} />}
        {onglet !== 'formateur' && !etudiant && <p>Choisissez d'abord votre nom dans la liste ci-dessus.</p>}
        {onglet === 'etudiant' && etudiant && <EcranEtudiant etudiant={etudiant} />}
        {onglet === 'relecteur' && etudiant && <p>Bonjour {etudiant.nom}. Vos relectures à faire.</p>}
      </section>
    </main>
  )
}

export default App
