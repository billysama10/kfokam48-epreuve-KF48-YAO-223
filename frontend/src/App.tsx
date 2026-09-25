import { useState } from 'react'

type Onglet = 'formateur' | 'etudiant' | 'relecteur'

const ONGLETS: { id: Onglet; libelle: string }[] = [
  { id: 'formateur', libelle: 'Formateur' },
  { id: 'etudiant', libelle: 'Étudiant' },
  { id: 'relecteur', libelle: 'Relecteur' },
]

function App() {
  const [onglet, setOnglet] = useState<Onglet>('formateur')

  return (
    <main>
      <h1>KF48 Présences et Relectures</h1>
      <nav>
        {ONGLETS.map((o) => (
          <button key={o.id} className={o.id === onglet ? 'actif' : ''} onClick={() => setOnglet(o.id)}>
            {o.libelle}
          </button>
        ))}
      </nav>
      <section>
        {onglet === 'formateur' && <p>Écran formateur : ouvrir une session, voir le tableau.</p>}
        {onglet === 'etudiant' && <p>Écran étudiant : marquer sa présence, déposer son exercice.</p>}
        {onglet === 'relecteur' && <p>Écran relecteur : faire une relecture.</p>}
      </section>
    </main>
  )
}

export default App
