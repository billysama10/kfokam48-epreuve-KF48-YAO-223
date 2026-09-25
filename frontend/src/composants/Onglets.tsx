export type OngletDef<T extends string> = { id: T; libelle: string; aide: string }

type Props<T extends string> = {
  onglets: OngletDef<T>[]
  actif: T
  onChange: (id: T) => void
}

/** Navigation par onglets : l'onglet actif est mis en évidence, chacun porte une courte aide. */
export function Onglets<T extends string>({ onglets, actif, onChange }: Props<T>) {
  return (
    <nav className="onglets" role="tablist" aria-label="Espaces">
      {onglets.map((o) => (
        <button
          key={o.id}
          type="button"
          role="tab"
          id={`onglet-${o.id}`}
          aria-selected={o.id === actif}
          aria-controls="panneau"
          className={o.id === actif ? 'onglet onglet-actif' : 'onglet'}
          onClick={() => onChange(o.id)}
        >
          <span className="onglet-libelle">{o.libelle}</span>
          <span className="onglet-aide">{o.aide}</span>
        </button>
      ))}
    </nav>
  )
}
