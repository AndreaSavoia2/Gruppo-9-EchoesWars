/**
 * Two circles "TU" (you) against the opponent (Simulation, Challenge waiting, Challenge accepted).
 * middle:          '• • •' or 'VS'
 * withWaves:       on mobile shows the waves around the opponent (we are waiting)
 * opponentFaded:   the opponent is lighter (has not answered yet / is playing)
 *
 * @param {object} props - the component props
 * @param {string} props.opponentInitials
 * @param {string} [props.middle='• • •']
 * @param {boolean} [props.withWaves=true]
 * @param {boolean} [props.opponentFaded=true]
 * @returns {JSX.Element}
 */
export default function Matchup({ opponentInitials, middle = '• • •', withWaves = true, opponentFaded = true }) {
  const vs = middle === 'VS'
  return (
    <div className="flex items-center justify-between px-5 lg:justify-center lg:gap-10 lg:py-10">
      <Avatar text="TU" />
      <span
        aria-hidden={!vs}
        className={`font-title lg:text-[36px] lg:text-ink lg:opacity-100 ${
          vs ? 'text-[44px] text-ink-secondary' : 'text-[28px] opacity-45'
        }`}
      >
        {middle}
      </span>
      <div className="relative">
        {withWaves && (
          <span aria-hidden="true" className="lg:hidden">
            <img src="/icons/waves/wave-3.svg" alt="" className="absolute top-1/2 left-1/2 size-42.5 max-w-none -translate-1/2 motion-safe:animate-pulse" />
            <img src="/icons/waves/wave-2.svg" alt="" className="absolute top-1/2 left-1/2 size-35 max-w-none -translate-1/2" />
            <img src="/icons/waves/wave-1.svg" alt="" className="absolute top-1/2 left-1/2 size-27.5 max-w-none -translate-1/2" />
          </span>
        )}
        <Avatar text={opponentInitials} faded={opponentFaded} />
      </div>
    </div>
  )
}

/**
 * Circle with the initials of a player (faded = still waiting).
 *
 * @param {object} props - the component props
 * @param {string} props.text
 * @param {boolean} [props.faded=false]
 * @returns {JSX.Element}
 */
function Avatar({ text, faded = false }) {
  return (
    <div
      className={`relative flex size-24 items-center justify-center rounded-full border-3 border-gold-border bg-gold font-title text-[34px] lg:size-35 lg:border-gold lg:bg-background-secondary lg:text-[28px] lg:opacity-100 ${
        faded ? 'opacity-55' : ''
      }`}
    >
      {text}
    </div>
  )
}
