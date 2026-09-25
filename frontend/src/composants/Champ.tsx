import type { ReactNode } from 'react'

type Props = {
  libelle: string
  /** Aide affichée sous le champ. */
  aide?: ReactNode
  /** Le champ lui-même : input, select ou textarea. */
  children: ReactNode
}

/** Champ de formulaire : libellé au-dessus, champ pleine largeur, aide en dessous. */
export function Champ({ libelle, aide, children }: Props) {
  return (
    <div className="champ">
      <label>
        <span className="champ-libelle">{libelle}</span>
        {children}
      </label>
      {aide && <span className="champ-aide">{aide}</span>}
    </div>
  )
}
