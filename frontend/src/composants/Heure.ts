/** Affiche une date ISO de l'API en heure locale, sans calcul métier. */
export function heure(iso: string): string {
  return new Date(iso).toLocaleTimeString('fr-FR', { hour: '2-digit', minute: '2-digit' })
}
