// Riga da scegliere (dal Figma "App/Opzione"): icona, titolo, dettaglio, freccia.
// Selezionata = sfondo oro + bordo spesso + spunta (non solo il colore).
export default function RigaSelezionabile({ titolo, dettaglio, scelto, onScegli, icona = '/icons/persona.svg', iconaDesktop = icona }) {
  return (
    <button
      type="button"
      onClick={onScegli}
      aria-pressed={scelto}
      className={`flex w-full items-center gap-3.5 rounded-2xl px-4 py-3.5 text-left ${
        scelto ? 'border-2 border-testo bg-oro' : 'border border-oro-bordo bg-sfondo hover:bg-sfondo-secondario'
      }`}
    >
      <span className="flex size-11 shrink-0 items-center justify-center rounded-xl bg-oro">
        <img src={icona} alt="" className="size-6 lg:hidden" />
        <img src={iconaDesktop} alt="" className="hidden size-6 lg:block" />
      </span>
      <span className="flex min-w-0 flex-1 flex-col gap-0.5 leading-[1.4]">
        <span className="truncate font-semibold">{titolo}</span>
        <span className="text-didascalia font-medium text-testo-secondario">{dettaglio}</span>
      </span>
      <img src={scelto ? '/icons/spunta.svg' : '/icons/freccia-destra.svg'} alt="" className="size-6 shrink-0" />
    </button>
  )
}
