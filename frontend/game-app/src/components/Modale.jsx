import { useEffect, useRef } from 'react'

/**
 * Finestra in primo piano con velo scuro dietro (es. "Convertire 10 copie?").
 * Si chiude con: tocco sul velo · tasto Esc · onChiudi chiamato da un bottone dentro.
 *
 * @param {object} props - le proprietà del componente
 * @param {string} props.titolo
 * @param {Function} props.onChiudi
 * @param {React.ReactNode} props.children
 * @returns {JSX.Element}
 */
export default function Modale({ titolo, onChiudi, children }) {
  const finestra = useRef(null)

  useEffect(() => {
    // Porta la tastiera dentro la finestra (sul primo campo o bottone)
    finestra.current?.querySelector('input, button')?.focus()

    const premi = (evento) => evento.key === 'Escape' && onChiudi()
    window.addEventListener('keydown', premi)
    return () => window.removeEventListener('keydown', premi)
  }, [onChiudi])

  return (
    <div className="fixed inset-0 z-40 flex items-center justify-center px-7.5">
      <button
        type="button"
        aria-label="Chiudi"
        onClick={onChiudi}
        className="absolute inset-0 bg-[rgba(34,37,44,0.6)] opacity-60"
      />
      <div
        ref={finestra}
        role="dialog"
        aria-modal="true"
        aria-labelledby="titolo-modale"
        className="relative flex w-full max-w-[342px] flex-col gap-3 rounded-[20px] bg-sfondo px-5 py-6 lg:max-w-md lg:p-8"
      >
        <h2 id="titolo-modale" className="font-titolo text-[22px] lg:text-h2">
          {titolo}
        </h2>
        {children}
      </div>
    </div>
  )
}
