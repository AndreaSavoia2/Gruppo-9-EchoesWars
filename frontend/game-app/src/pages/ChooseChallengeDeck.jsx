import { useCallback } from 'react'
import { useNavigate, useParams } from 'react-router'
import DeckPicker from '../components/DeckPicker'
import { useFetch } from '../hooks/useFetch'
import { getChallenge, chooseChallengeDeck } from '../api/challenges'

/**
 * A2 / DA2 · /challenge/:id/deck — the request has been sent, in the meantime choose the deck
 *
 * @returns {JSX.Element}
 */
export default function ChooseChallengeDeck() {
  const { id } = useParams()
  const navigate = useNavigate()
  const loadChallenge = useCallback((signal) => getChallenge(id, signal), [id])
  const { data: challenge } = useFetch(loadChallenge)
  const friendName = challenge?.friend.name ?? 'il tuo amico'

  /** Saves the chosen deck for the challenge. */
  async function confirm(deck) {
    await chooseChallengeDeck(id, deck.id)
    navigate(`/challenge/${id}`) // "In attesa" (waiting) screen
  }

  return (
    <DeckPicker
      subtitle={`✓ Richiesta inviata a ${friendName}. Scegli il mazzo mentre aspetti: la partita parte quando accetta.`}
      mobileNotice={`Richiesta inviata a ${friendName}. Scegli il mazzo mentre aspetti.`}
      back={{ path: '/play/friend', text: 'Amici', mobileText: 'Amici' }}
      buttonText="Conferma il mazzo"
      onConfirm={confirm}
    />
  )
}
