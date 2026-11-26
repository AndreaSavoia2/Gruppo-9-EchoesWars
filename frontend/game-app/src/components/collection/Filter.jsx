import { useState } from 'react'

/**
 * A drop-down filter (from the Figma "Galleria/Filtro" + "Galleria/Menu filtro").
 * label: "Scuola" · options: ["Guerrieri", "Maghi", ...] · chosen: the applied values
 * isOpen / onOpen / onClose: the parent decides which filter is open (one at a time)
 * onApply(values): confirms the choice
 * single = true → only ONE value can be chosen (radio dots, e.g. "Ordina"); title = text at the top of the menu
 *
 * @param {object} props - the component props
 * @param {string} props.label
 * @param {string} [props.title=label]
 * @param {boolean} [props.single=false]
 * @param {string[]} [props.options=[]]
 * @param {string[]} props.chosen
 * @param {boolean} props.isOpen
 * @param {Function} props.onOpen
 * @param {Function} props.onClose
 * @param {Function} props.onApply
 * @returns {JSX.Element}
 */
export default function Filter({ label, title = label, single = false, options = [], chosen, isOpen, onOpen, onClose, onApply }) {
  // "Trial" choices while the menu is open: they become real only with "Applica" (apply)
  const [draft, setDraft] = useState(chosen)

  /** Opens the menu, starting from the applied values. */
  function openMenu() {
    setDraft(chosen)
    onOpen()
  }

  /** Adds or removes a value from the draft (only one value if single). */
  function toggle(value) {
    if (single) return setDraft([value])
    setDraft((before) => (before.includes(value) ? before.filter((v) => v !== value) : [...before, value]))
  }

  // Button text: "Scuola" · "Scuola: Maghi" · "Scuola: Maghi +1"
  let text = label
  if (chosen.length > 0) text = `${label}: ${chosen[0]}${chosen.length > 1 ? ` +${chosen.length - 1}` : ''}`

  let styleName = 'border border-ink bg-background' // closed
  if (chosen.length > 0) styleName = 'border-[1.5px] border-ink bg-gold' // applied
  if (isOpen) styleName = 'border-2 border-ink bg-background-secondary' // open

  return (
    <div className="lg:relative">
      <button
        type="button"
        onClick={isOpen ? onClose : openMenu}
        aria-expanded={isOpen}
        className={`flex min-h-11 items-center gap-2 rounded-full pr-3.5 pl-4.5 font-medium lg:min-h-12 ${styleName}`}
      >
        {text}
        <img src={isOpen ? '/icons/arrow-up.svg' : '/icons/arrow-down.svg'} alt="" className="size-4.5" />
      </button>

      {isOpen && (
        <div
          role="dialog"
          aria-label={`Filtro ${label}`}
          className="absolute inset-x-0 top-full z-30 mt-1 flex flex-col gap-3 rounded-2xl border border-gold-border bg-background p-5 shadow-[0_8px_12px_rgba(33,38,43,0.15)] lg:right-auto lg:w-max lg:min-w-[300px]"
        >
          <p className="leading-[1.4] font-semibold text-ink-secondary">{title}</p>

          {/* Many options (e.g. Abilities) → 2 columns */}
          <div className={`grid gap-x-1 ${options.length > 6 ? 'grid-cols-2' : 'grid-cols-1'}`}>
            {options.map((option) => {
              const isSelected = draft.includes(option)
              return (
                <label
                  key={option}
                  className={`flex min-h-11 cursor-pointer items-center gap-3 rounded-[14px] px-2 ${
                    isSelected ? 'bg-background-secondary font-semibold' : ''
                  }`}
                >
                  {/* The real checkbox is hidden: we draw the Figma one ourselves */}
                  <input
                    type={single ? 'radio' : 'checkbox'}
                    name={single ? `filter-${label}` : undefined}
                    checked={isSelected}
                    onChange={() => toggle(option)}
                    className="peer sr-only"
                  />
                  {single ? (
                    // Radio dot (single choice)
                    <span className="flex size-5.5 shrink-0 items-center justify-center rounded-full border-[1.5px] border-ink bg-background peer-focus-visible:outline-2">
                      {isSelected && <span className="size-3 rounded-full bg-ink" />}
                    </span>
                  ) : (
                    // Checkbox (multiple choice)
                    <span
                      className={`flex size-5.5 shrink-0 items-center justify-center rounded peer-focus-visible:outline-2 ${
                        isSelected ? 'bg-ink' : 'border-[1.5px] border-ink bg-background'
                      }`}
                    >
                      {isSelected && <img src="/icons/checkbox.svg" alt="" className="size-5.5" />}
                    </span>
                  )}
                  {option}
                </label>
              )
            })}
          </div>

          <div className="flex items-center gap-4">
            <button
              type="button"
              onClick={() => onApply(draft)}
              className="rounded-full border border-ink bg-gold px-8 py-3 font-medium shadow-[0_6px_8px_rgba(0,0,0,0.15)]"
            >
              Applica
            </button>
            {/* "Azzera" (reset) is not needed when only one value can be chosen (e.g. Ordina) */}
            {!single && (
              <button type="button" onClick={() => onApply([])} className="font-medium text-ink-secondary underline">
                Azzera
              </button>
            )}
          </div>
        </div>
      )}
    </div>
  )
}
