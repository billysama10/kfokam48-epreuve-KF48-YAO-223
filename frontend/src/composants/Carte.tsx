import type { ReactNode } from 'react'

type Props = {
  titre?: ReactNode
  /** Courte phrase qui explique à quoi sert la carte. */
  aide?: ReactNode
  /** Bouton ou badge affiché à droite du titre. */
  action?: ReactNode
  niveau?: 2 | 3
  className?: string
  children?: ReactNode
}

/** Bloc blanc arrondi qui regroupe un titre, une aide et son contenu. */
export function Carte({ titre, aide, action, niveau = 2, className, children }: Props) {
  const Titre = niveau === 3 ? 'h3' : 'h2'
  return (
    <section className={className ? `carte ${className}` : 'carte'}>
      {(titre || action) && (
        <div className="carte-entete">
          <div>
            {titre && <Titre>{titre}</Titre>}
            {aide && <p className="carte-aide">{aide}</p>}
          </div>
          {action}
        </div>
      )}
      {children}
    </section>
  )
}
