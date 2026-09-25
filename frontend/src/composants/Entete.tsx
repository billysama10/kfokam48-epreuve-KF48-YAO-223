/** En-tête : nom de l'application et rôle en une phrase. */
export function Entete() {
  return (
    <header className="entete">
      <span className="entete-logo" aria-hidden="true">
        KF
      </span>
      <div>
        <h1>KF48 Présences et Relectures</h1>
        <p className="entete-sous-titre">
          Le formateur donne un code de présence, les étudiants le saisissent, déposent leur exercice et se
          relisent entre pairs.
        </p>
      </div>
    </header>
  )
}
