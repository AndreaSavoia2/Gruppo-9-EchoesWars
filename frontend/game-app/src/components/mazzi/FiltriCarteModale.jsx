import { useEffect, useState } from 'react'
import { FASCE_COSTO, NESSUN_FILTRO, filtraCarte } from './filtriCarte'

const ORDINE_RARITA = ['Comune', 'Rara', 'Leggendaria']
const ABILITA_VISIBILI = 6

// 5t · "Filtra la collezione": pannello che sale dal basso (mobile) o finestra al centro (desktop)
export default function FiltriCarteModale({ carte, filtri, onApplica, onChiudi }) {
  const [bozza, setBozza] = useState(filtri)
  const [tutteAbilita, setTutteAbilita] = useState(false)

  // I valori possibili li ricaviamo dalle carte che hai
  const unici = (campo) => [...new Set(carte.map((carta) => carta[campo]))]
  const sezioni = [
    { chiave: 'scuola', titolo: 'Scuola', opzioni: unici('scuola').sort() },
    { chiave: 'rarita', titolo: 'Rarità', opzioni: ORDINE_RARITA.filter((r) => unici('rarita').includes(r)) },
    { chiave: 'costo', titolo: 'Costo', opzioni: FASCE_COSTO.map((f) => f.etichetta) },
    { chiave: 'abilita', titolo: 'Abilità', opzioni: unici('abilita').sort() },
  ]

  const risultati = filtraCarte(carte, bozza).length

  useEffect(() => {
    const premi = (evento) => evento.key === 'Escape' && onChiudi()
    window.addEventListener('keydown', premi)
    return () => window.removeEventListener('keydown', premi)
  }, [onChiudi])

  function cambia(chiave, valore) {
    const valori = bozza[chiave]
    setBozza({ ...bozza, [chiave]: valori.includes(valore) ? valori.filter((v) => v !== valore) : [...valori, valore] })
  }

  return (
    <div className="fixed inset-0 z-40">
      <button type="button" aria-label="Chiudi i filtri" onClick={onChiudi} className="absolute inset-0 bg-[rgba(34,37,44,0.6)] opacity-60" />
      <div
        role="dialog"
        aria-modal="true"
        aria-labelledby="titolo-filtri"
        className="absolute inset-x-0 bottom-0 flex max-h-[90vh] flex-col rounded-t-[28px] bg-sfondo lg:inset-x-auto lg:top-1/2 lg:bottom-auto lg:left-1/2 lg:w-[560px] lg:-translate-1/2 lg:rounded-[28px]"
      >
        <div className="flex flex-col gap-4 overflow-y-auto px-5 pt-3 pb-4">
          <span aria-hidden="true" className="mx-auto h-1 w-10 rounded bg-oro-bordo lg:hidden" />
          <div className="flex items-center justify-between">
            <h2 id="titolo-filtri" className="font-titolo text-[22px]">
              Filtra la collezione
            </h2>
            <button type="button" onClick={onChiudi} aria-label="Chiudi" className="flex size-11 items-center justify-center rounded-full border-[1.5px] border-testo text-xl">
              ✕
            </button>
          </div>

          {sezioni.map(({ chiave, titolo, opzioni }) => {
            const troppe = chiave === 'abilita' && opzioni.length > ABILITA_VISIBILI && !tutteAbilita
            const visibili = troppe ? opzioni.slice(0, ABILITA_VISIBILI) : opzioni
            return (
              <fieldset key={chiave} className="flex flex-col gap-1">
                <legend className="mb-1 leading-[1.4] font-semibold">
                  {titolo}
                  {bozza[chiave].length > 0 && ` (${bozza[chiave].length})`}
                </legend>
                <div className="grid grid-cols-2 gap-x-1">
                  {visibili.map((opzione) => {
                    const selezionata = bozza[chiave].includes(opzione)
                    return (
                      <label
                        key={opzione}
                        className={`flex min-h-11 cursor-pointer items-center gap-3 rounded-[14px] px-2 ${selezionata ? 'bg-sfondo-secondario font-semibold' : ''}`}
                      >
                        <input type="checkbox" checked={selezionata} onChange={() => cambia(chiave, opzione)} className="peer sr-only" />
                        <span
                          className={`flex size-5.5 shrink-0 items-center justify-center rounded peer-focus-visible:outline-2 ${
                            selezionata ? 'bg-testo' : 'border-[1.5px] border-testo bg-sfondo'
                          }`}
                        >
                          {selezionata && <img src="/icons/casella-spunta.svg" alt="" className="size-5.5" />}
                        </span>
                        {opzione}
                      </label>
                    )
                  })}
                </div>
                {troppe && (
                  <button type="button" onClick={() => setTutteAbilita(true)} className="self-start font-medium underline">
                    Mostra tutte ({opzioni.length})
                  </button>
                )}
              </fieldset>
            )
          })}
        </div>

        <div className="flex gap-3 border-t border-oro px-5 pt-3 pb-6">
          <button type="button" onClick={() => setBozza(NESSUN_FILTRO)} className="flex min-h-11 flex-1 items-center justify-center rounded-full border-[1.5px] border-testo font-medium">
            Azzera
          </button>
          <button type="button" onClick={() => onApplica(bozza)} className="flex min-h-11 flex-1 items-center justify-center rounded-full bg-oro-bordo font-medium">
            Mostra {risultati} {risultati === 1 ? 'carta' : 'carte'}
          </button>
        </div>
      </div>
    </div>
  )
}
