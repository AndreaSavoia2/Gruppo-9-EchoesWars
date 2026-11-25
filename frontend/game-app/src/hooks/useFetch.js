import { useCallback, useEffect, useState } from 'react'

/**
 * Hook riutilizzabile: chiama una funzione di fetch e ti dà 4 cose:
 *   dati        → la risposta del backend (null finché non arriva)
 *   caricamento → true mentre aspettiamo la PRIMA risposta
 *   errore      → l'errore, se qualcosa va storto
 *   ricarica    → funzione per rifare la richiesta (es. dopo una modifica)
 *
 * Uso: const { dati, caricamento, errore, ricarica } = useFetch(getDashboard)
 *
 * @param {*} funzioneFetch
 */
export function useFetch(funzioneFetch) {
  const [stato, setStato] = useState({ dati: null, caricamento: true, errore: null })
  // Ogni volta che "versione" cambia, la richiesta viene rifatta
  const [versione, setVersione] = useState(0)

  useEffect(() => {
    const controller = new AbortController()

    funzioneFetch(controller.signal)
      .then((dati) => setStato({ dati, caricamento: false, errore: null }))
      .catch((errore) => {
        // AbortError = richiesta annullata da noi: non è un vero errore
        if (errore.name !== 'AbortError') {
          setStato({ dati: null, caricamento: false, errore })
        }
      })

    // Se il componente sparisce, annulla la richiesta
    return () => controller.abort()
  }, [funzioneFetch, versione])

  const ricarica = useCallback(() => setVersione((v) => v + 1), [])

  return { ...stato, ricarica }
}
