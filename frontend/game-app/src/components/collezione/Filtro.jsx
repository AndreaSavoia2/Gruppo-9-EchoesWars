import { useState } from 'react'

// Un filtro a tendina (dal Figma "Galleria/Filtro" + "Galleria/Menu filtro").
// etichetta: "Scuola" · opzioni: ["Guerrieri", "Maghi", ...] · scelti: i valori applicati
// aperto / onApri / onChiudi: il genitore decide quale filtro è aperto (uno alla volta)
// onApplica(valori): conferma la scelta
// singola = true → si sceglie UN solo valore (pallini, es. "Ordina"); titolo = testo in cima al menu
export default function Filtro({ etichetta, titolo = etichetta, singola = false, opzioni = [], scelti, aperto, onApri, onChiudi, onApplica }) {
  // Scelte "in prova" mentre il menu è aperto: diventano vere solo con "Applica"
  const [bozza, setBozza] = useState(scelti)

  function apri() {
    setBozza(scelti)
    onApri()
  }

  function cambia(valore) {
    if (singola) return setBozza([valore])
    setBozza((prima) => (prima.includes(valore) ? prima.filter((v) => v !== valore) : [...prima, valore]))
  }

  // Testo del bottone: "Scuola" · "Scuola: Maghi" · "Scuola: Maghi +1"
  let testo = etichetta
  if (scelti.length > 0) testo = `${etichetta}: ${scelti[0]}${scelti.length > 1 ? ` +${scelti.length - 1}` : ''}`

  let stile = 'border border-testo bg-sfondo' // chiuso
  if (scelti.length > 0) stile = 'border-[1.5px] border-testo bg-oro' // applicato
  if (aperto) stile = 'border-2 border-testo bg-sfondo-secondario' // aperto

  return (
    <div className="lg:relative">
      <button
        type="button"
        onClick={aperto ? onChiudi : apri}
        aria-expanded={aperto}
        className={`flex min-h-11 items-center gap-2 rounded-full pr-3.5 pl-4.5 font-medium lg:min-h-12 ${stile}`}
      >
        {testo}
        <img src={aperto ? '/icons/freccia-su.svg' : '/icons/freccia-giu.svg'} alt="" className="size-4.5" />
      </button>

      {aperto && (
        <div
          role="dialog"
          aria-label={`Filtro ${etichetta}`}
          className="absolute inset-x-0 top-full z-30 mt-1 flex flex-col gap-3 rounded-2xl border border-oro-bordo bg-sfondo p-5 shadow-[0_8px_12px_rgba(33,38,43,0.15)] lg:right-auto lg:w-max lg:min-w-[300px]"
        >
          <p className="leading-[1.4] font-semibold text-testo-secondario">{titolo}</p>

          {/* Tante opzioni (es. Abilità) → 2 colonne */}
          <div className={`grid gap-x-1 ${opzioni.length > 6 ? 'grid-cols-2' : 'grid-cols-1'}`}>
            {opzioni.map((opzione) => {
              const selezionata = bozza.includes(opzione)
              return (
                <label
                  key={opzione}
                  className={`flex min-h-11 cursor-pointer items-center gap-3 rounded-[14px] px-2 ${
                    selezionata ? 'bg-sfondo-secondario font-semibold' : ''
                  }`}
                >
                  {/* La casella vera è nascosta: disegniamo noi quella del Figma */}
                  <input
                    type={singola ? 'radio' : 'checkbox'}
                    name={singola ? `filtro-${etichetta}` : undefined}
                    checked={selezionata}
                    onChange={() => cambia(opzione)}
                    className="peer sr-only"
                  />
                  {singola ? (
                    // Pallino (scelta singola)
                    <span className="flex size-5.5 shrink-0 items-center justify-center rounded-full border-[1.5px] border-testo bg-sfondo peer-focus-visible:outline-2">
                      {selezionata && <span className="size-3 rounded-full bg-testo" />}
                    </span>
                  ) : (
                    // Casella (scelta multipla)
                    <span
                      className={`flex size-5.5 shrink-0 items-center justify-center rounded peer-focus-visible:outline-2 ${
                        selezionata ? 'bg-testo' : 'border-[1.5px] border-testo bg-sfondo'
                      }`}
                    >
                      {selezionata && <img src="/icons/casella-spunta.svg" alt="" className="size-5.5" />}
                    </span>
                  )}
                  {opzione}
                </label>
              )
            })}
          </div>

          <div className="flex items-center gap-4">
            <button
              type="button"
              onClick={() => onApplica(bozza)}
              className="rounded-full border border-testo bg-oro px-8 py-3 font-medium shadow-[0_6px_8px_rgba(0,0,0,0.15)]"
            >
              Applica
            </button>
            {/* "Azzera" non serve quando si sceglie un solo valore (es. Ordina) */}
            {!singola && (
              <button type="button" onClick={() => onApplica([])} className="font-medium text-testo-secondario underline">
                Azzera
              </button>
            )}
          </div>
        </div>
      )}
    </div>
  )
}
