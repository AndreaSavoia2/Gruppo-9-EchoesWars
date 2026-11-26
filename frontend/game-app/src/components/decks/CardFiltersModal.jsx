import { useEffect, useState } from 'react'
import { COST_RANGES, NO_FILTERS, filterCards } from './cardFilters'

const RARITY_ORDER = ['Comune', 'Rara', 'Leggendaria']
const VISIBLE_ABILITIES = 6

/**
 * 5t · "Filtra la collezione": panel that slides up from the bottom (mobile) or window in the middle (desktop)
 *
 * @param {object} props - the component props
 * @param {object[]} props.cardList - the cards to filter
 * @param {object} props.filters - the filters applied now
 * @param {Function} props.onApply
 * @param {Function} props.onClose
 * @returns {JSX.Element}
 */
export default function CardFiltersModal({ cardList, filters, onApply, onClose }) {
  const [draft, setDraft] = useState(filters)
  const [allAbilities, setAllAbilities] = useState(false)

  // The possible values are taken from the cards you own
  const unique = (field) => [...new Set(cardList.map((card) => card[field]))]
  const sections = [
    { key: 'school', title: 'Scuola', options: unique('school').sort() },
    { key: 'rarity', title: 'Rarità', options: RARITY_ORDER.filter((r) => unique('rarity').includes(r)) },
    { key: 'cost', title: 'Costo', options: COST_RANGES.map((f) => f.label) },
    { key: 'ability', title: 'Abilità', options: unique('ability').sort() },
  ]

  const results = filterCards(cardList, draft).length

  useEffect(() => {
    const onKey = (event) => event.key === 'Escape' && onClose()
    window.addEventListener('keydown', onKey)
    return () => window.removeEventListener('keydown', onKey)
  }, [onClose])

  /** Adds or removes a value from one section of the draft. */
  function toggle(key, value) {
    const values = draft[key]
    setDraft({ ...draft, [key]: values.includes(value) ? values.filter((v) => v !== value) : [...values, value] })
  }

  return (
    <div className="fixed inset-0 z-40">
      <button type="button" aria-label="Chiudi i filtri" onClick={onClose} className="absolute inset-0 bg-[rgba(34,37,44,0.6)] opacity-60" />
      <div
        role="dialog"
        aria-modal="true"
        aria-labelledby="filters-title"
        className="absolute inset-x-0 bottom-0 flex max-h-[90vh] flex-col rounded-t-[28px] bg-background lg:inset-x-auto lg:top-1/2 lg:bottom-auto lg:left-1/2 lg:w-[560px] lg:-translate-1/2 lg:rounded-[28px]"
      >
        <div className="flex flex-col gap-4 overflow-y-auto px-5 pt-3 pb-4">
          <span aria-hidden="true" className="mx-auto h-1 w-10 rounded bg-gold-border lg:hidden" />
          <div className="flex items-center justify-between">
            <h2 id="filters-title" className="font-title text-[22px]">
              Filtra la collezione
            </h2>
            <button type="button" onClick={onClose} aria-label="Chiudi" className="flex size-11 items-center justify-center rounded-full border-[1.5px] border-ink text-xl">
              ✕
            </button>
          </div>

          {sections.map(({ key, title, options }) => {
            const tooMany = key === 'ability' && options.length > VISIBLE_ABILITIES && !allAbilities
            const visibleItems = tooMany ? options.slice(0, VISIBLE_ABILITIES) : options
            return (
              <fieldset key={key} className="flex flex-col gap-1">
                <legend className="mb-1 leading-[1.4] font-semibold">
                  {title}
                  {draft[key].length > 0 && ` (${draft[key].length})`}
                </legend>
                <div className="grid grid-cols-2 gap-x-1">
                  {visibleItems.map((option) => {
                    const isSelected = draft[key].includes(option)
                    return (
                      <label
                        key={option}
                        className={`flex min-h-11 cursor-pointer items-center gap-3 rounded-[14px] px-2 ${isSelected ? 'bg-background-secondary font-semibold' : ''}`}
                      >
                        <input type="checkbox" checked={isSelected} onChange={() => toggle(key, option)} className="peer sr-only" />
                        <span
                          className={`flex size-5.5 shrink-0 items-center justify-center rounded peer-focus-visible:outline-2 ${
                            isSelected ? 'bg-ink' : 'border-[1.5px] border-ink bg-background'
                          }`}
                        >
                          {isSelected && <img src="/icons/checkbox.svg" alt="" className="size-5.5" />}
                        </span>
                        {option}
                      </label>
                    )
                  })}
                </div>
                {tooMany && (
                  <button type="button" onClick={() => setAllAbilities(true)} className="self-start font-medium underline">
                    Mostra tutte ({options.length})
                  </button>
                )}
              </fieldset>
            )
          })}
        </div>

        <div className="flex gap-3 border-t border-gold px-5 pt-3 pb-6">
          <button type="button" onClick={() => setDraft(NO_FILTERS)} className="flex min-h-11 flex-1 items-center justify-center rounded-full border-[1.5px] border-ink font-medium">
            Azzera
          </button>
          <button type="button" onClick={() => onApply(draft)} className="flex min-h-11 flex-1 items-center justify-center rounded-full bg-gold-border font-medium">
            Mostra {results} {results === 1 ? 'carta' : 'carte'}
          </button>
        </div>
      </div>
    </div>
  )
}
