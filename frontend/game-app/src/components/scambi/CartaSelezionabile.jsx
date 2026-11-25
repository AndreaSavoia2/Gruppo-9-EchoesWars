import { coloreRarita } from '../../data/rarita'

const motivi = { mazzo: '🔒 In mazzo', riservata: '⏳ Riservata' }

// Carta da scegliere nella "Nuova proposta". Bloccata = velo scuro + motivo, non si può toccare.
export default function CartaSelezionabile({ carta, scelta, onScegli }) {
  const bloccata = Boolean(carta.bloccata)
  return (
    <button
      type="button"
      onClick={onScegli}
      disabled={bloccata}
      aria-pressed={scelta}
      className="flex w-full flex-col items-start gap-1 text-left text-didascalia leading-[1.4] font-medium disabled:cursor-not-allowed"
    >
      <span className={`relative block aspect-[107/118] w-full overflow-hidden rounded-lg ${scelta ? 'border-3 border-testo' : ''}`}>
        {/* Miniatura ritagliata: mostra solo l'illustrazione (o la vecchia immagine intera) */}
        <img src={carta.illustrazione ?? carta.immagine} alt="" className="size-full object-cover" />
        {scelta && (
          <span className="absolute top-0.75 left-0.75 rounded-full border border-testo bg-oro px-2 py-0.75">✓ Scelta</span>
        )}
        {bloccata && (
          <>
            <span className="absolute inset-0 bg-[rgba(33,38,43,0.55)]" />
            <span className="absolute bottom-1.5 left-1.5 rounded-lg bg-sfondo px-1.5 py-0.75">{motivi[carta.bloccata]}</span>
          </>
        )}
      </span>
      <span>{carta.nome}</span>
      <span className={coloreRarita[carta.rarita]}>
        <span aria-hidden="true">◆ </span>
        {carta.rarita}
      </span>
    </button>
  )
}
