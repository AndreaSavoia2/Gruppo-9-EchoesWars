import { useEffect, useRef } from 'react'
import ImmagineCarta from '../carta/ImmagineCarta'

// 6e / D6c · Carta ingrandita: velo scuro + bottone "Chiudi" + carta grande + nome.
// Si chiude con: bottone Chiudi · tocco sul velo · tasto Esc.
export default function CartaIngrandita({ carta, onChiudi }) {
  const chiudi = useRef(null)

  useEffect(() => {
    // Porta la tastiera sul bottone "Chiudi"
    chiudi.current?.focus()
    const premi = (evento) => evento.key === 'Escape' && onChiudi()
    window.addEventListener('keydown', premi)
    return () => window.removeEventListener('keydown', premi)
  }, [onChiudi])

  return (
    <div className="fixed inset-0 z-40 flex items-center justify-center px-5">
      {/* Velo scuro: toccandolo si chiude */}
      <button
        type="button"
        aria-label="Chiudi"
        tabIndex={-1}
        onClick={onChiudi}
        className="absolute inset-0 bg-testo opacity-85"
      />

      <div role="dialog" aria-modal="true" aria-label={`Carta ingrandita: ${carta.nome}`} className="relative flex flex-col items-end gap-3.5 lg:gap-4">
        <button
          ref={chiudi}
          type="button"
          onClick={onChiudi}
          className="flex min-h-11 w-30 items-center justify-center rounded-full border-[1.5px] border-testo bg-sfondo font-medium lg:w-32.5"
        >
          ✕ Chiudi
        </button>

        {/* La carta tiene sempre le proporzioni 560×785.
            Mobile: larga al massimo 362px (o lo schermo meno i margini).
            Desktop: alta al massimo 785px, ma rimpicciolita se lo schermo è basso. */}
        <ImmagineCarta
          carta={carta}
          className="aspect-[560/785] w-[min(362px,calc(100vw-40px))] rounded-lg object-cover lg:h-[min(785px,calc(100vh-140px))] lg:w-auto lg:rounded-3xl"
        />

        <p className="w-full text-center leading-[1.4] font-semibold text-sfondo">{carta.nome}</p>
      </div>
    </div>
  )
}
