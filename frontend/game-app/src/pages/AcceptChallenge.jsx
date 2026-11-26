import { useNavigate, useParams, useSearchParams } from 'react-router'
import DeckPicker from '../components/DeckPicker'
import { acceptRequest } from '../api/requests'

/**
 * R7 / D15b · /requests/:id/deck — you accepted a challenge: choose the deck and the match starts
 *
 * @returns {JSX.Element}
 */
export default function AcceptChallenge() {
  const { id } = useParams()
  const [params] = useSearchParams()
  const navigate = useNavigate()
  const challenger = params.get('from') ?? 'il tuo amico'

  /** Accepts the challenge with the chosen deck and opens the match. */
  async function start(deck) {
    const { matchId } = await acceptRequest(id, deck.id)
    navigate(`/match/${matchId}`, { state: { isNew: true } })
  }

  return (
    <DeckPicker
      subtitle={`✓ Hai accettato la sfida di ${challenger}. Scegli il mazzo: la partita parte subito.`}
      mobileNotice={`Hai accettato la sfida di ${challenger}. Scegli il mazzo: la partita parte subito.`}
      back={{ path: '/requests', text: 'Richieste', mobileText: 'Richieste' }}
      buttonText="Inizia la partita"
      onConfirm={start}
    />
  )
}
