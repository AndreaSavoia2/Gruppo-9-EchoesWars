/**
 * Progress bar (from the Figma "App/Barra progresso") with the value written next to it (e.g. 1/2).
 *
 * @param {object} props - the component props
 * @param {number} props.progress
 * @param {number} props.total
 * @returns {JSX.Element}
 */
export default function ProgressBar({ progress, total }) {
  const percent = Math.min(100, (progress / total) * 100)
  return (
    <div className="flex items-center gap-2">
      <div
        role="progressbar"
        aria-valuenow={progress}
        aria-valuemin={0}
        aria-valuemax={total}
        className="h-2 flex-1 overflow-hidden rounded border border-ink-secondary bg-background-secondary"
      >
        <div className="h-full bg-gold-border" style={{ width: `${percent}%` }} />
      </div>
      <span className="text-caption leading-[1.4] font-medium">
        {progress}/{total}
      </span>
    </div>
  )
}
