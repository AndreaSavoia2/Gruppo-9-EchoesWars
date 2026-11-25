import { useNavigate } from 'react-router'
import SceltaMazzo from '../components/SceltaMazzo'
import { avviaPartitaCasuale } from '../api/partite'

// G3 / DG2 · Partita casuale → scegli il mazzo → la partita parte
export default function ScegliMazzoCasuale() {
  const navigate = useNavigate()

  async function iniziaPartita(mazzo) {
    const partita = await avviaPartitaCasuale(mazzo.id)
    // Vai alla schermata "Simulazione in corso" di questa partita
    navigate(`/partita/${partita.id}`, { state: { nuova: true } })
  }

  return (
    <SceltaMazzo
      sottotitolo="Partita casuale · scegli con quale mazzo giocare."
      indietro={{ percorso: '/gioca', testo: 'Come vuoi giocare?' }}
      testoBottone="Inizia la partita"
      onConferma={iniziaPartita}
    />
  )
}
