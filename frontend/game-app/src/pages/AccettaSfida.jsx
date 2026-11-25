import { useNavigate, useParams, useSearchParams } from 'react-router'
import SceltaMazzo from '../components/SceltaMazzo'
import { accettaRichiesta } from '../api/richieste'

/**
 * R7 / D15b · /richieste/:id/mazzo — hai accettato una sfida: scegli il mazzo e si parte
 *
 * @returns {JSX.Element}
 */
export default function AccettaSfida() {
  const { id } = useParams()
  const [parametri] = useSearchParams()
  const navigate = useNavigate()
  const sfidante = parametri.get('da') ?? 'il tuo amico'

  async function inizia(mazzo) {
    const { partitaId } = await accettaRichiesta(id, mazzo.id)
    navigate(`/partita/${partitaId}`, { state: { nuova: true } })
  }

  return (
    <SceltaMazzo
      sottotitolo={`✓ Hai accettato la sfida di ${sfidante}. Scegli il mazzo: la partita parte subito.`}
      avvisoMobile={`Hai accettato la sfida di ${sfidante}. Scegli il mazzo: la partita parte subito.`}
      indietro={{ percorso: '/richieste', testo: 'Richieste', testoMobile: 'Richieste' }}
      testoBottone="Inizia la partita"
      onConferma={inizia}
    />
  )
}
