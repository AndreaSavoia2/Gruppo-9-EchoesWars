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

/**
 * Scarica i dati dell'utente una volta sola e li rende disponibili a tutta l'app.
 *
 * @param {object} props - le proprietà del componente
 * @param {React.ReactNode} props.children
 * @returns {JSX.Element}
 */
export function UtenteProvider({ children }) {
  const statoUtente = useFetch(getUtente)
  const [benvenutoVisto, setBenvenutoVisto] = useState(false)
  const valore = { ...statoUtente, benvenutoVisto, segnaBenvenutoVisto: () => setBenvenutoVisto(true) }
  return <UtenteContext.Provider value={valore}>{children}</UtenteContext.Provider>
}

/**
 * Legge i dati dell'utente condivisi (dati, caricamento, errore, ricarica...).
 */
export function useUtente() {
  return useContext(UtenteContext)
}
