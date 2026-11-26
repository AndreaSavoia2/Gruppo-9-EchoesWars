import { useState } from 'react'
import { Link, useNavigate } from 'react-router'
import AppLayout from '../components/AppLayout'
import PageHeader from '../components/PageHeader'
import Button from '../components/Button'
import { echoes } from '../data/echoes'
import { DECK_RULES } from '../data/rules'

/**
 * 5q · 5r · /collection/decks/new — Step 1: choose the 2 Echoes
 *
 * @returns {JSX.Element}
 */
export default function NewDeck() {
  const navigate = useNavigate()
  const [chosen, setChosen] = useState([])
  const full = chosen.length === DECK_RULES.echoes
  const missing = DECK_RULES.echoes - chosen.length

  /** Selects or deselects an Echo (at most 2). */
  function toggleEcho(name) {
    if (chosen.includes(name)) setChosen(chosen.filter((n) => n !== name))
    else if (!full) setChosen([...chosen, name])
  }

  /** Goes to step 2 with the chosen Echoes in the address. */
  function next() {
    // The chosen Echoes travel in the address: /collection/decks/new/cards?echo=Sapienza&echo=Oblio
    const params = new URLSearchParams(chosen.map((echo) => ['echo', echo]))
    navigate(`/collection/decks/new/cards?${params}`)
  }

  return (
    <AppLayout activePage="collection" mobileBackground="/images/deck-background.png" desktopBackground="/images/deck-background-desktop.png">
      <div className="flex flex-col gap-3 lg:max-w-3xl lg:gap-5">
        <PageHeader title="Nuovo mazzo" subtitle="Passo 1 di 2 · Scegli 2 Echi" />
        <Link to="/collection/decks" className="mt-3 self-start font-medium underline lg:mt-0">
          ‹ Mazzi
        </Link>
        <p>Un mazzo usa solo carte di 2 Echi. Tocca due riquadri: potrai cambiarli finché non salvi.</p>

        <ul className="grid grid-cols-2 gap-5 lg:grid-cols-4">
          {echoes.map((echo) => {
            const chosenOne = chosen.includes(echo.name)
            const disabledLook = full && !chosenOne
            return (
              <li key={echo.name}>
                <button
                  type="button"
                  onClick={() => toggleEcho(echo.name)}
                  disabled={disabledLook}
                  aria-pressed={chosenOne}
                  className={`flex aspect-square w-full flex-col items-center justify-center gap-1.5 rounded-[18px] leading-[1.4] ${
                    chosenOne ? 'border-3 border-ink bg-background-secondary' : 'border border-gold bg-background'
                  } ${disabledLook ? 'cursor-not-allowed opacity-45' : ''}`}
                >
                  <span className={`h-1 w-10 rounded-sm ${echo.background}`} />
                  <img src={echo.icon} alt="" className="size-18 object-contain" />
                  <span className="font-semibold">{echo.name}</span>
                  <span className="text-caption font-medium">
                    {chosenOne ? '✓ Scelto' : disabledLook ? `Hai già ${DECK_RULES.echoes} Echi` : echo.description}
                  </span>
                </button>
              </li>
            )
          })}
        </ul>

        <p className="leading-[1.4] font-semibold" aria-live="polite">
          {chosen.length} di {DECK_RULES.echoes} Echi scelti{chosen.length > 0 && `: ${chosen.join(' + ')}`}
        </p>
        <Button onClick={next} disabled={!full} className="w-full lg:w-auto lg:min-w-60 lg:self-start">
          {full ? 'Continua' : `Scegli ancora ${missing} ${missing === 1 ? 'Eco' : 'Echi'}`}
        </Button>
      </div>
    </AppLayout>
  )
}
