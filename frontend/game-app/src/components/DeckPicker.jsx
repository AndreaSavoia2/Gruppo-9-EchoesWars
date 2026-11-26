import { useState } from 'react'
import { Link } from 'react-router'
import AppLayout from './AppLayout'
import PageHeader from './PageHeader'
import MatchDeck from './MatchDeck'
import Button from './Button'
import Panel from './Panel'
import Skeleton from './Skeleton'
import ErrorMessage from './ErrorMessage'
import { useFetch } from '../hooks/useFetch'
import { getDecks } from '../api/decks'

/**
 * "Scegli il mazzo" (choose the deck) screen: used both by the random match and by the friend challenge.
 * subtitle:    text under the title (desktop only)
 * back:        { path, text, mobileText } for the "‹ ..." link at the top
 * mobileNotice: text of the purple box under the title (mobile only, optional)
 * buttonText:  e.g. "Inizia la partita"
 * onConfirm:   function called with the chosen deck when the button is pressed
 *
 * @param {object} props - the component props
 * @param {string} props.subtitle
 * @param {{ path: string, text: string, mobileText?: string }} props.back
 * @param {string} [props.mobileNotice]
 * @param {string} props.buttonText
 * @param {Function} props.onConfirm
 * @returns {JSX.Element}
 */
export default function DeckPicker({ subtitle, back, mobileNotice, buttonText, onConfirm }) {
  const { data, loading, error } = useFetch(getDecks)
  const decks = data?.decks
  const [chosenId, setChosenId] = useState(null)
  const [sending, setSending] = useState({ inProgress: false, error: null })

  const chosenDeck = decks?.find((m) => m.id === chosenId)

  /** Calls onConfirm with the chosen deck. */
  async function confirm() {
    setSending({ inProgress: true, error: null })
    try {
      await onConfirm(chosenDeck)
    } catch {
      setSending({ inProgress: false, error: 'Qualcosa è andato storto. Riprova.' })
    }
  }

  const legend = (
    <p className="text-caption leading-[1.4] font-medium">🔒 = mazzo non valido: sistemalo in Collezione › Mazzi.</p>
  )

  return (
    <AppLayout activePage="play" mobileBackground="/images/deck-background.png" desktopBackground="/images/deck-background-desktop.png" hideMobileMenu>
      <div className="flex flex-1 flex-col gap-3 lg:gap-6">
        {/* 📱 Mobile: "‹ Indietro" (back) above the title */}
        <Link to={back.path} className="-mt-6 self-start font-medium underline lg:hidden">
          ‹ {back.mobileText ?? 'Indietro'}
        </Link>

        <PageHeader title="Scegli il mazzo">
          <p className="hidden lg:block">{subtitle}</p>
          <Link to={back.path} className="hidden font-medium underline lg:block">
            ‹ {back.text}
          </Link>
        </PageHeader>

        {mobileNotice && (
          <p className="flex gap-2.5 rounded-xl border border-info bg-info-background px-3.5 py-3 text-caption leading-[1.4] font-medium lg:hidden">
            <span aria-hidden="true" className="text-body font-semibold">✓</span>
            {mobileNotice}
          </p>
        )}

        <div className="flex flex-1 items-start gap-6">
          {/* List of decks: inside a panel on desktop */}
          <section className="flex flex-1 flex-col gap-3 lg:gap-4 lg:self-stretch lg:rounded-2xl lg:border lg:border-gold lg:bg-background lg:p-6">
            <h2 className="hidden font-title text-h2 lg:block">I tuoi mazzi</h2>
            {legend}

            {loading && (
              <div className="grid grid-cols-3 gap-x-2.5 gap-y-3 lg:flex lg:flex-wrap lg:gap-5">
                {[1, 2, 3, 4, 5, 6].map((n) => (
                  <Skeleton key={n} className="h-58 rounded-2xl lg:w-35" />
                ))}
              </div>
            )}
            {error && <ErrorMessage />}
            {decks?.length === 0 && <p>Non hai ancora mazzi. Creane uno in Collezione › Mazzi.</p>}

            {decks?.length > 0 && (
              <div className="grid grid-cols-3 gap-x-2.5 gap-y-3 lg:flex lg:flex-wrap lg:gap-5">
                {decks.map((deck) => (
                  <MatchDeck
                    key={deck.id}
                    deck={deck}
                    chosenOne={deck.id === chosenId}
                    onChoose={() => setChosenId(deck.id)}
                  />
                ))}
              </div>
            )}
          </section>

          {/* 💻 Desktop: summary on the right */}
          <div className="hidden w-85 shrink-0 lg:block">
            <Panel title="Mazzo scelto">
              {chosenDeck ? (
                <>
                  <p className="leading-[1.4] font-semibold">{chosenDeck.name}</p>
                  <p>
                    {chosenDeck.cardCount}/{chosenDeck.maxCards} carte · {chosenDeck.points}/{chosenDeck.maxPoints} punti
                    {chosenDeck.schools?.length > 0 && ` · ${chosenDeck.schools.join(' + ')}`}
                  </p>
                </>
              ) : (
                <p>Nessun mazzo scelto.</p>
              )}
              <Button className="min-h-12 w-full" onClick={confirm} disabled={!chosenDeck || sending.inProgress}>
                {sending.inProgress ? 'Attendi…' : buttonText}
              </Button>
              {sending.error && <ErrorMessage text={sending.error} />}
            </Panel>
          </div>
        </div>
      </div>

      {/* 📱 Mobile: fixed bar at the bottom with the button */}
      <div className="fixed inset-x-0 bottom-0 z-10 flex flex-col gap-1.5 border border-gold bg-background px-5 pt-3 pb-6 lg:hidden">
        <p className="truncate leading-[1.4] font-semibold">
          {chosenDeck ? `Scelto: ${chosenDeck.name}` : 'Scegli un mazzo'}
        </p>
        <Button onClick={confirm} disabled={!chosenDeck || sending.inProgress}>
          {sending.inProgress ? 'Attendi…' : buttonText}
        </Button>
        {sending.error && <ErrorMessage text={sending.error} />}
      </div>
    </AppLayout>
  )
}
