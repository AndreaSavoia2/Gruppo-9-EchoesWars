import { useEffect, useRef } from 'react'
import CardImage from '../card/CardImage'

/**
 * 6e / D6c · Enlarged card: dark veil + "Chiudi" (close) button + big card + name.
 * It closes with: Close button · tap on the veil · Esc key.
 *
 * @param {object} props - the component props
 * @param {object} props.card
 * @param {Function} props.onClose
 * @returns {JSX.Element}
 */
export default function EnlargedCard({ card, onClose }) {
  const close = useRef(null)

  useEffect(() => {
    // Moves the keyboard focus onto the "Chiudi" button
    close.current?.focus()
    const onKey = (event) => event.key === 'Escape' && onClose()
    window.addEventListener('keydown', onKey)
    return () => window.removeEventListener('keydown', onKey)
  }, [onClose])

  return (
    <div className="fixed inset-0 z-40 flex items-center justify-center px-5">
      {/* Dark veil: tapping it closes the window */}
      <button
        type="button"
        aria-label="Chiudi"
        tabIndex={-1}
        onClick={onClose}
        className="absolute inset-0 bg-ink opacity-85"
      />

      <div role="dialog" aria-modal="true" aria-label={`Carta ingrandita: ${card.name}`} className="relative flex flex-col items-end gap-3.5 lg:gap-4">
        <button
          ref={close}
          type="button"
          onClick={onClose}
          className="flex min-h-11 w-30 items-center justify-center rounded-full border-[1.5px] border-ink bg-background font-medium lg:w-32.5"
        >
          ✕ Chiudi
        </button>

        {/* The card always keeps the 560×785 proportions.
            Mobile: at most 362px wide (or the screen minus the margins).
            Desktop: at most 785px high, but smaller if the screen is short. */}
        <CardImage
          card={card}
          className="aspect-[560/785] w-[min(362px,calc(100vw-40px))] rounded-lg object-cover lg:h-[min(785px,calc(100vh-140px))] lg:w-auto lg:rounded-3xl"
        />

        <p className="w-full text-center leading-[1.4] font-semibold text-background">{card.name}</p>
      </div>
    </div>
  )
}
