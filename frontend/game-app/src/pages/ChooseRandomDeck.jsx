import { useNavigate } from 'react-router'
import DeckPicker from '../components/DeckPicker'
import { startRandomMatch } from '../api/matches'

/**
 * G3 / DG2 · Random match → choose the deck → the match starts
 *
 * @returns {JSX.Element}
 */
export default function ChooseRandomDeck() {
  const navigate = useNavigate()

  /** Starts a random match with the chosen deck. */
  async function beginMatch(deck) {
    const match = await startRandomMatch(deck.id)
    // Go to the "Simulazione in corso" (simulation in progress) screen of this match
    navigate(`/match/${match.id}`, { state: { isNew: true } })
  }

  return (
    <DeckPicker
      subtitle="Partita casuale · scegli con quale mazzo giocare."
      back={{ path: '/play', text: 'Come vuoi giocare?' }}
      buttonText="Inizia la partita"
      onConfirm={beginMatch}
    />
  )
}
