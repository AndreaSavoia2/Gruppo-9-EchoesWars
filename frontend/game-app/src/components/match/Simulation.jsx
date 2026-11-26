import Matchup from './Matchup'
import { initials } from '../../utils/text'

/**
 * G4 / DG3 · "Stiamo simulando la partita" (simulating the match): YOU against the opponent.
 *
 * @param {object} props - the component props
 * @param {object} props.match
 * @returns {JSX.Element}
 */
export default function Simulation({ match }) {
  return (
    <div className="flex flex-col gap-6" aria-live="polite">
      <div className="flex flex-col gap-2.5 text-center lg:gap-1.5 lg:text-left">
        <h1 className="font-title text-[22px] lg:text-h2">Stiamo simulando la partita</h1>
        <p>
          {match.myDeck} contro il mazzo di {match.opponent.name}. Ci vogliono pochi secondi: non devi fare
          nulla.
          <span className="hidden lg:inline"> Il risultato arriva anche se chiudi l’app.</span>
        </p>
      </div>

      <div className="mt-16 lg:mt-0">
        <Matchup opponentInitials={initials(match.opponent.name)} />
      </div>
    </div>
  )
}
