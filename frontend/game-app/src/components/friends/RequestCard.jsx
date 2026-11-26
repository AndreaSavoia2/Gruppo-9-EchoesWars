import InitialsAvatar from './InitialsAvatar'
import Button from '../Button'
import { howLongAgo } from '../../utils/text'

const types = {
  friendship: { text: 'Vuole diventare tuo amico', label: '👥 Amicizia', styleName: 'border-gold bg-background-secondary', accept: 'Accetta' },
  challenge: { text: 'Ti sfida a una partita', label: '⚔ Sfida', styleName: 'border-info bg-info-background', accept: 'Accetta e scegli il mazzo' },
}

/**
 * A request (from the Figma "App/Richiesta"). The kind is written, not only coloured.
 * compact = true → small version for the right panel in Friends (desktop)
 *
 * @param {object} props - the component props
 * @param {object} props.request
 * @param {Function} props.onAccept
 * @param {Function} props.onDecline
 * @param {boolean} [props.compact=false]
 * @returns {JSX.Element}
 */
export default function RequestCard({ request, onAccept, onDecline, compact = false }) {
  const kind = types[request.kind]
  return (
    <article className="flex flex-col gap-3 rounded-2xl border border-gold-border bg-background p-3.5">
      <div className="flex items-center gap-3">
        <InitialsAvatar name={request.from.name} />
        <div className="flex min-w-0 flex-1 flex-col gap-0.5 leading-[1.4]">
          <h3 className="truncate font-semibold">{request.from.name}</h3>
          <p className="text-caption font-medium">{kind.text}</p>
        </div>
        {!compact && (
          <span className={`shrink-0 rounded-full border px-2.5 py-1 text-caption leading-[1.4] font-medium ${kind.styleName}`}>
            {kind.label}
          </span>
        )}
      </div>
      {!compact && <p className="text-caption leading-[1.4] font-medium">{howLongAgo(request.receivedOn)}</p>}
      <div className="flex gap-2.5">
        <Button onClick={onAccept} className="flex-1">
          {kind.accept}
        </Button>
        <Button variant="secondary" onClick={onDecline} className={request.kind === 'challenge' ? 'w-auto' : 'flex-1'}>
          Rifiuta
        </Button>
      </div>
    </article>
  )
}
