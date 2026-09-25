import { chargerTableau } from '../api/tableau'
import type { Promotion } from '../api/types'
import { useAppel } from '../api/useAppel'
import { Badge } from '../composants/Badge'
import { Carte } from '../composants/Carte'
import { Chargement, EtatVide, MessageErreur } from '../composants/Etat'

/**
 * EF8, EF15 : tableau du formateur. La moyenne et son caractère provisoire sont affichés
 * tels que l'API les renvoie (F3, RG18, RG23), sans aucun recalcul.
 */
export function Tableau({ promotion }: { promotion: Promotion }) {
  const tableau = useAppel(() => chargerTableau(promotion.id), [promotion.id])

  return (
    <Carte
      titre={`Tableau de la promotion ${promotion.nom}`}
      aide="Présences, exercices déposés, moyenne reçue (calculée par le serveur) et relectures à faire."
      action={
        <button type="button" className="bouton-secondaire" onClick={tableau.recharger} disabled={tableau.chargement}>
          {tableau.chargement ? 'Chargement…' : 'Actualiser'}
        </button>
      }
    >
      <div className="pile">
        {tableau.chargement && <Chargement />}
        <MessageErreur erreur={tableau.erreur} />
        {tableau.donnees?.length === 0 && <EtatVide>Aucun étudiant dans cette promotion.</EtatVide>}
        {tableau.donnees && tableau.donnees.length > 0 && (
          <>
            <div className="tableau-defile">
              <table className="tableau">
                <thead>
                  <tr>
                    <th scope="col">Étudiant</th>
                    <th scope="col" className="nombre">
                      Présences
                    </th>
                    <th scope="col" className="nombre">
                      Exercices déposés
                    </th>
                    <th scope="col">Moyenne reçue</th>
                    <th scope="col">Relectures à faire</th>
                  </tr>
                </thead>
                <tbody>
                  {tableau.donnees.map((l) => (
                    <tr key={l.etudiantId}>
                      <th scope="row">{l.nom}</th>
                      <td className="nombre">{l.presences}</td>
                      <td className="nombre">{l.exercicesDeposes}</td>
                      <td>
                        <span className="cellule-moyenne">
                          {l.moyenne === null ? (
                            <span className="tiret" aria-label="aucune note">
                              —
                            </span>
                          ) : (
                            <strong>{`${l.moyenne}/20`}</strong>
                          )}
                          {l.moyenneProvisoire && <Badge variante="provisoire">provisoire</Badge>}
                        </span>
                      </td>
                      <td>
                        {l.relecturesEnAttente > 0 ? (
                          <Badge variante="attente">{`${l.relecturesEnAttente} à faire`}</Badge>
                        ) : (
                          <span className="tiret">0</span>
                        )}
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
            <p className="legende">
              <Badge variante="provisoire">provisoire</Badge> une seule des deux relectures d'un exercice est rendue
              pour l'instant.
            </p>
          </>
        )}
      </div>
    </Carte>
  )
}
