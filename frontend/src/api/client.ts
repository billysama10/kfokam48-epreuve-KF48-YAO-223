// Couche d'accès à l'API (F3) : tous les appels HTTP passent par ce fichier.

/** Erreur renvoyée par l'API au format imposé { code, message }. */
export class ApiError extends Error {
  readonly code: string
  readonly statut: number

  constructor(statut: number, code: string, message: string) {
    super(message)
    this.statut = statut
    this.code = code
  }
}

type Options = {
  methode?: 'GET' | 'POST' | 'PUT'
  corps?: unknown
  entetes?: Record<string, string>
}

export async function appeler<T>(chemin: string, options: Options = {}): Promise<T> {
  let reponse: Response
  try {
    reponse = await fetch(chemin, {
      method: options.methode ?? 'GET',
      headers: {
        ...(options.corps !== undefined ? { 'Content-Type': 'application/json' } : {}),
        ...options.entetes,
      },
      body: options.corps !== undefined ? JSON.stringify(options.corps) : undefined,
    })
  } catch {
    throw new ApiError(0, 'RESEAU', 'Le serveur est injoignable. Le backend est-il démarré ?')
  }

  const texte = await reponse.text()
  const donnees = texte ? JSON.parse(texte) : undefined

  if (!reponse.ok) {
    const code = donnees?.code ?? 'ERREUR_INCONNUE'
    const message = donnees?.message ?? `Erreur ${reponse.status}`
    throw new ApiError(reponse.status, code, message)
  }
  return donnees as T
}
