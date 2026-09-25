import { listerRelectures } from '../api/relectures'
import type { Etudiant } from '../api/types'
import { useAppel } from '../api/useAppel'
import { Chargement, MessageErreur } from '../composants/Etat'

/** Écran relecteur (F2) : les exercices qui me sont attribués (EF6). */
export function EcranRelecteur({ etudiant }: { etudiant: Etudiant }) {
  const relectures = useAppel(() => listerRelectures(etudiant.id), [etudiant.id])

  return (
    <div>
      <h2>Mes relectures</h2>
      <p>
        <button onClick={relectures.recharger}>Actualiser</button>
      </p>
      {relectures.chargement && <Chargement />}
      <MessageErreur erreur={relectures.erreur} />
      {relectures.donnees?.length === 0 && <p>Aucun exercice ne vous est attribué pour le moment.</p>}
      {relectures.donnees?.map((r) => (
        <article key={r.id} className="relecture">
          <h3>
            {r.sessionTitre} <small>(relecture n° {r.id})</small>
          </h3>
          <p>
            Exercice à relire :{' '}
            <a href={r.lien} target="_blank" rel="noreferrer">
              {r.lien}
            </a>
          </p>
          {r.rendue ? (
            <p className="succes">
              Rendue : {r.note}/20 — {r.commentaire}
            </p>
          ) : (
            <p>À faire.</p>
          )}
        </article>
      ))}
    </div>
  )
}
