import { chargerTableau } from '../api/tableau'
import type { Promotion } from '../api/types'
import { useAppel } from '../api/useAppel'
import { Chargement, MessageErreur } from '../composants/Etat'

/**
 * EF8, EF15 : tableau du formateur. La moyenne et son caractère provisoire sont affichés
 * tels que l'API les renvoie (F3, RG18, RG23), sans aucun recalcul.
 */
export function Tableau({ promotion }: { promotion: Promotion }) {
  const tableau = useAppel(() => chargerTableau(promotion.id), [promotion.id])

  return (
    <div>
      <h2>Tableau de la promotion {promotion.nom}</h2>
      <p>
        <button onClick={tableau.recharger}>Actualiser</button>
      </p>
      {tableau.chargement && <Chargement />}
      <MessageErreur erreur={tableau.erreur} />
      {tableau.donnees && (
        <table>
          <thead>
            <tr>
              <th>Étudiant</th>
              <th>Présences</th>
              <th>Exercices déposés</th>
              <th>Moyenne reçue</th>
              <th>Relectures à faire</th>
            </tr>
          </thead>
          <tbody>
            {tableau.donnees.map((l) => (
              <tr key={l.etudiantId}>
                <td>{l.nom}</td>
                <td>{l.presences}</td>
                <td>{l.exercicesDeposes}</td>
                <td>
                  {l.moyenne === null ? '—' : `${l.moyenne}/20`}
                  {l.moyenneProvisoire && <em className="attente"> (provisoire)</em>}
                </td>
                <td className={l.relecturesEnAttente > 0 ? 'attente' : ''}>{l.relecturesEnAttente}</td>
              </tr>
            ))}
          </tbody>
        </table>
      )}
    </div>
  )
}
