import { useCallback } from 'react'
import { useNavigate, useParams } from 'react-router'
import SceltaMazzo from '../components/SceltaMazzo'
import { useFetch } from '../hooks/useFetch'
import { getSfida, scegliMazzoSfida } from '../api/sfide'

// A2 / DA2 · /sfida/:id/mazzo — la richiesta è partita, intanto scegli il mazzo
export default function ScegliMazzoSfida() {
  const { id } = useParams()
  const navigate = useNavigate()
  const caricaSfida = useCallback((signal) => getSfida(id, signal), [id])
  const { dati: sfida } = useFetch(caricaSfida)
  const nomeAmico = sfida?.amico.nome ?? 'il tuo amico'

  async function conferma(mazzo) {
    await scegliMazzoSfida(id, mazzo.id)
    navigate(`/sfida/${id}`) // schermata "In attesa"
  }

  return (
    <SceltaMazzo
      sottotitolo={`✓ Richiesta inviata a ${nomeAmico}. Scegli il mazzo mentre aspetti: la partita parte quando accetta.`}
      avvisoMobile={`Richiesta inviata a ${nomeAmico}. Scegli il mazzo mentre aspetti.`}
      indietro={{ percorso: '/gioca/amico', testo: 'Amici', testoMobile: 'Amici' }}
      testoBottone="Conferma il mazzo"
      onConferma={conferma}
    />
  )
}
