import { Link } from 'react-router'

// A history row: outcome (icon + word) + opponent + round.
// path (optional): the row becomes a link (e.g. to the match details)
const styles = {
  victory: {
    box: 'bg-success-background border-success text-success',
    icon: '/icons/victory.svg',
    text: 'Vittoria',
  },
  defeat: {
    box: 'bg-error-background border-error text-error',
    icon: '/icons/defeat.svg',
    text: 'Sconfitta',
  },
  draw: {
    box: 'bg-info-background border-info text-info',
    icon: '/icons/draw.svg',
    text: 'Pareggio',
  },
}

/**
 * Row of a match in the lists (Home, History): outcome, opponent and round.
 *
 * @param {object} props - the component props
 * @param {'victory'|'defeat'|'draw'} props.outcome
 * @param {string} props.opponent
 * @param {number} props.round
 * @param {string} [props.path]
 * @param {boolean} [props.withVs=false]
 * @returns {JSX.Element}
 */
export default function MatchRow({ outcome, opponent, round, path, withVs = false }) {
  const styleName = styles[outcome]

  const Wrapper = path ? Link : 'div'
  return (
    <Wrapper to={path} className="flex min-h-18 w-full items-center gap-3 rounded-2xl border border-gold-border bg-background p-3">
      <div
        className={`flex h-14 w-18 shrink-0 flex-col items-center justify-center gap-0.5 rounded-xl border-[1.5px] ${styleName.box}`}
      >
        <img src={styleName.icon} alt="" className="size-5" />
        <span className="text-caption leading-[1.4] font-medium">{styleName.text}</span>
      </div>
      <div className="flex min-w-0 flex-col gap-0.5 leading-[1.4]">
        <p className="truncate font-semibold">{withVs ? `vs ${opponent}` : opponent}</p>
        <p className="text-caption font-medium">{round} round</p>
      </div>
    </Wrapper>
  )
}
