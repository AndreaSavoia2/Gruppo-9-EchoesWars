import { createContext, useContext, useState } from 'react'
import { useFetch } from '../hooks/useFetch'
import { getUtente } from '../api/utente'

// Il "contesto" è una scatola condivisa: i dati dell'utente
// vengono scaricati UNA volta e qualsiasi componente può leggerli
// con useUtente(), senza passarli a mano di componente in componente.
// Contiene anche "ricarica": utile quando i crediti cambiano (es. dopo una partita).
// benvenutoVisto / segnaBenvenutoVisto: ricordano che l'utente ha appena finito
// (o saltato) il benvenuto, così non ci torna mentre il backend si aggiorna.
const UtenteContext = createContext(null)

export function UtenteProvider({ children }) {
  const statoUtente = useFetch(getUtente)
  const [benvenutoVisto, setBenvenutoVisto] = useState(false)
  const valore = { ...statoUtente, benvenutoVisto, segnaBenvenutoVisto: () => setBenvenutoVisto(true) }
  return <UtenteContext.Provider value={valore}>{children}</UtenteContext.Provider>
}

export function useUtente() {
  return useContext(UtenteContext)
}
