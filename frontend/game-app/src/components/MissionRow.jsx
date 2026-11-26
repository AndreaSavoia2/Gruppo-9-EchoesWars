import ProgressBar from './ProgressBar'

/**
 * An "Echo Trial" (from the Figma "App/Riga missione").
 * States: In progress · Completed (Claim button) · Claimed (green ✓)
 *
 * @param {object} props - the component props
 * @param {string} props.title
 * @param {string} props.reward
 * @param {number} props.progress
 * @param {number} props.total
 * @param {boolean} [props.claimed=false]
 * @param {Function} props.onClaim
 * @param {boolean} [props.inProgress=false]
 * @returns {JSX.Element}
 */
export default function MissionRow({ title, reward, progress, total, claimed = false, onClaim, inProgress = false }) {
  const completed = progress >= total
  return (
    <div
      className={`flex w-full items-center gap-3 rounded-2xl border border-gold-border py-3.5 pr-3 pl-4 leading-[1.4] ${
        claimed ? 'bg-background-secondary' : 'bg-background'
      }`}
    >
      <div className="flex min-w-0 flex-1 flex-col gap-1.5">
        <p className="font-semibold">{title}</p>
        <p className="text-caption font-medium text-ink-secondary">Ricompensa: {reward}</p>
        <ProgressBar progress={progress} total={total} />
      </div>
      {claimed && <span className="shrink-0 font-semibold text-success">✓ Riscossa</span>}
      {completed && !claimed && onClaim && (
        <button
          type="button"
          onClick={onClaim}
          disabled={inProgress}
          className="flex min-h-11 shrink-0 items-center rounded-full bg-gold-border px-5 font-medium disabled:opacity-50"
        >
          Riscuoti
        </button>
      )}
    </div>
  )
}
