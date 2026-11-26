import { useEffect, useRef } from 'react'

/**
 * Window in the foreground with a dark veil behind (e.g. "Convertire 10 copie?").
 * It closes with: tap on the veil · Esc key · onClose called by a button inside.
 *
 * @param {object} props - the component props
 * @param {string} props.title
 * @param {Function} props.onClose
 * @param {React.ReactNode} props.children
 * @returns {JSX.Element}
 */
export default function Modal({ title, onClose, children }) {
  const popup = useRef(null)

  useEffect(() => {
    // Moves the keyboard focus inside the window (on the first field or button)
    popup.current?.querySelector('input, button')?.focus()

    const onKey = (event) => event.key === 'Escape' && onClose()
    window.addEventListener('keydown', onKey)
    return () => window.removeEventListener('keydown', onKey)
  }, [onClose])

  return (
    <div className="fixed inset-0 z-40 flex items-center justify-center px-7.5">
      <button
        type="button"
        aria-label="Chiudi"
        onClick={onClose}
        className="absolute inset-0 bg-[rgba(34,37,44,0.6)] opacity-60"
      />
      <div
        ref={popup}
        role="dialog"
        aria-modal="true"
        aria-labelledby="modal-title"
        className="relative flex w-full max-w-[342px] flex-col gap-3 rounded-[20px] bg-background px-5 py-6 lg:max-w-md lg:p-8"
      >
        <h2 id="modal-title" className="font-title text-[22px] lg:text-h2">
          {title}
        </h2>
        {children}
      </div>
    </div>
  )
}
