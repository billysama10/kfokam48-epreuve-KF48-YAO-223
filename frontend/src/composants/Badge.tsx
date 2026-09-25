import type { ReactNode } from 'react'

type Variante = 'provisoire' | 'attente' | 'succes' | 'neutre'

/** Petite pastille colorée : provisoire (orange), attente, succès (vert), neutre (gris). */
export function Badge({ variante, children }: { variante: Variante; children: ReactNode }) {
  return <span className={`badge badge-${variante}`}>{children}</span>
}
