import type { ReactNode } from 'react'
import { listerRelectures } from '../api/relectures'
import type { Etudiant, RelectureAttribuee } from '../api/types'
import { useAppel } from '../api/useAppel'
import { Badge } from '../composants/Badge'
import { Carte } from '../composants/Carte'
import { Chargement, EtatVide, MessageErreur } from '../composants/Etat'
import { FormulaireRelecture } from './FormulaireRelecture'

/** Écran relecteur (F2) : les exercices qui me sont attribués (EF6) et leur relecture (EF7). */
export function EcranRelecteur({ etudiant }: { etudiant: Etudiant }) {
  const relectures = useAppel(() => listerRelectures(etudiant.id), [etudiant.id])
  // Séparation d'affichage selon le champ « rendue » renvoyé par l'API.
  const aFaire = relectures.donnees?.filter((r) => !r.rendue) ?? []
  const rendues = relectures.donnees?.filter((r) => r.rendue) ?? []

  return (
    <div className="pile">
      <div className="entete-section">
        <div>
          <h2>Mes relectures</h2>
          <p className="carte-aide">Ouvrez le lien, lisez l'exercice, puis donnez une note et un commentaire.</p>
        </div>
        <button
          type="button"
          className="bouton-secondaire"
          onClick={relectures.recharger}
          disabled={relectures.chargement}
        >
          {relectures.chargement ? 'Chargement…' : 'Actualiser'}
        </button>
      </div>
      {relectures.chargement && <Chargement />}
      <MessageErreur erreur={relectures.erreur} />
      {relectures.donnees && (
        <>
          <section className="pile" aria-label="Relectures à faire">
            <h3 className="titre-liste">
              À faire <Badge variante={aFaire.length > 0 ? 'attente' : 'neutre'}>{aFaire.length}</Badge>
            </h3>
            {aFaire.length === 0 && <EtatVide>Aucune relecture à faire.</EtatVide>}
            {aFaire.map((r) => (
              <CarteRelecture key={r.id} relecture={r}>
                <FormulaireRelecture relecture={r} relecteurId={etudiant.id} onRendue={relectures.recharger} />
              </CarteRelecture>
            ))}
          </section>
          {rendues.length > 0 && (
            <section className="pile" aria-label="Relectures rendues">
              <h3 className="titre-liste">
                Déjà rendues <Badge variante="neutre">{rendues.length}</Badge>
              </h3>
              {rendues.map((r) => (
                <CarteRelecture key={r.id} relecture={r}>
                  <div className="relecture-rendue">
                    <span className="relecture-note">{r.note}/20</span>
                    <p>{r.commentaire}</p>
                  </div>
                </CarteRelecture>
              ))}
            </section>
          )}
        </>
      )}
    </div>
  )
}

function CarteRelecture({ relecture, children }: { relecture: RelectureAttribuee; children: ReactNode }) {
  return (
    <Carte
      niveau={3}
      className={relecture.rendue ? 'carte-relecture carte-rendue' : 'carte-relecture'}
      titre={relecture.sessionTitre}
      aide={`Relecture n° ${relecture.id}`}
      action={
        relecture.rendue ? <Badge variante="succes">Rendue</Badge> : <Badge variante="attente">À faire</Badge>
      }
    >
      <div className="pile">
        <a className="lien-exercice" href={relecture.lien} target="_blank" rel="noreferrer">
          <span className="lien-exercice-libelle">
            {relecture.rendue ? 'Exercice relu' : 'Exercice à relire'}
          </span>
          <span className="lien-exercice-url">{relecture.lien}</span>
        </a>
        {children}
      </div>
    </Carte>
  )
}
