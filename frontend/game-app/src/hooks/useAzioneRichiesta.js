import { useState } from 'react'
import { useNavigate } from 'react-router'
import { accettaRichiesta, rifiutaRichiesta } from '../api/richieste'

// Logica condivisa tra Richieste e Amici: accetta / rifiuta e mostra un messaggio.
// Per le SFIDE "Accetta" porta alla scelta del mazzo.
export function useAzioneRichiesta(ricarica) {
  const navigate = useNavigate()
  const [messaggio, setMessaggio] = useState(null) // { tipo: 'successo' | 'info' | 'errore', testo }

  async function accetta(richiesta) {
    if (richiesta.tipo === 'sfida') {
      navigate(`/richieste/${richiesta.id}/mazzo?da=${encodeURIComponent(richiesta.da.nome)}`)
      return
    }
    try {
      await accettaRichiesta(richiesta.id)
      setMessaggio({ tipo: 'successo', testo: `${richiesta.da.nome} è ora tuo amico. Lo trovi in Amici.` })
      ricarica()
    } catch {
      setMessaggio({ tipo: 'errore', testo: 'Operazione non riuscita. Riprova.' })
    }
  }

  async function rifiuta(richiesta) {
    try {
      await rifiutaRichiesta(richiesta.id)
      setMessaggio({ tipo: 'info', testo: `Hai rifiutato la richiesta di ${richiesta.da.nome}. Non riceverà nessuna notifica.` })
      ricarica()
    } catch {
      setMessaggio({ tipo: 'errore', testo: 'Operazione non riuscita. Riprova.' })
    }
  }

  return { messaggio, accetta, rifiuta }
}
