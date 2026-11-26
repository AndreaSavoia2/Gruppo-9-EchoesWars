import { useCallback, useMemo, useState } from 'react'
import { Link, Navigate, useNavigate, useParams, useSearchParams } from 'react-router'
import AppLayout from '../components/AppLayout'
import Button from '../components/Button'
import Modal from '../components/Modal'
import Skeleton from '../components/Skeleton'
import ErrorMessage from '../components/ErrorMessage'
import CardFiltersModal from '../components/decks/CardFiltersModal'
import { NO_FILTERS, filterCards, countFilters } from '../components/decks/cardFilters'
import { useFetch } from '../hooks/useFetch'
import { getAvailableCards, getDeck, saveDeck } from '../api/decks'
import { findEcho } from '../data/echoes'
import { DECK_RULES } from '../data/rules'
import CardImage from '../components/card/CardImage'

/**
 * 5d · 5e · 5n · 5s · 5t / D5
 *   /collection/decks/new/cards?echo=Sapienza&echo=Oblio → new deck (step 2)
 *   /collection/decks/:id                                  → edits an existing deck
 *
 * @returns {JSX.Element}
 */
export default function DeckEditor() {
  const { id } = useParams()
  const [params] = useSearchParams()

  // Edit: first we download the saved deck
  const loadDeck = useCallback((signal) => (id ? getDeck(id, signal) : Promise.resolve(null)), [id])
  const { data: deck, loading, error } = useFetch(loadDeck)

  if (!id) {
    const echoes = params.getAll('echo')
    // Without 2 Echoes the deck cannot be built: back to step 1
    if (echoes.length !== DECK_RULES.echoes) return <Navigate to="/collection/decks/new" replace />
    return <Editor echoes={echoes} />
  }

  return loading || error ? (
    <AppLayout activePage="collection" mobileBackground="/images/deck-background.png" desktopBackground="/images/deck-background-desktop.png">
      {loading && <Skeleton className="h-40 rounded-2xl" />}
      {error && <ErrorMessage text="Impossibile caricare il mazzo. Riprova più tardi." />}
    </AppLayout>
  ) : (
    <Editor id={id} echoes={deck.echoes} initialName={deck.name} initialCards={deck.cardList} />
  )
}

/**
 * The deck editor: available cards, cards in the deck, counters and saving.
 *
 * @param {object} props - the component props
 * @param {string|number} [props.id] - id of the deck to edit (none = new deck)
 * @param {string[]} props.echoes
 * @param {string} [props.initialName='']
 * @param {{ cardId: number, copies: number }[]} [props.initialCards=[]]
 * @returns {JSX.Element}
 */
function Editor({ id, echoes, initialName = '', initialCards = [] }) {
  const navigate = useNavigate()
  // echoes.join() = "Sapienza,Oblio": this way the request starts again only if the Echoes really change
  const echoesKey = echoes.join()
  const loadCards = useCallback((signal) => getAvailableCards(echoesKey.split(','), signal), [echoesKey])
  const { data: cardList, loading, error } = useFetch(loadCards)

  // The cards in the deck: { idOfCard: copies }. The insertion order is the order in the tray.
  const [inDeck, setInDeck] = useState(() =>
    Object.fromEntries(initialCards.map(({ cardId, copies }) => [cardId, copies])),
  )
  const [filters, setFilters] = useState(NO_FILTERS)
  const [filtersOpen, setFiltersOpen] = useState(false)
  const [nameOpen, setNameOpen] = useState(false)
  const [name, setName] = useState(initialName)
  const [saving, setSaving] = useState({ inProgress: false, error: null })

  // Finds a card by id
  const byId = useMemo(() => Object.fromEntries((cardList ?? []).map((card) => [card.id, card])), [cardList])

  // Deck counts
  const rows = Object.entries(inDeck)
    .map(([idOfCard, copies]) => ({ card: byId[idOfCard], copies }))
    .filter((row) => row.card)
  const totalCards = rows.reduce((sum, r) => sum + r.copies, 0)
  const totalPoints = rows.reduce((sum, r) => sum + r.copies * r.card.points, 0)
  const missing = DECK_RULES.cards - totalCards
  const pointsOver = totalPoints - DECK_RULES.points
  const valid = missing === 0 && pointsOver <= 0

  // The tray: one box for every copy (e.g. ×2 → 2 boxes)
  const tray = rows.flatMap(({ card, copies }) => Array.from({ length: copies }, () => card))

  // How many copies of a card you can still add
  const copyLimit = (card) => Math.min(card.amount, DECK_RULES.copies)

  /** Adds one copy of a card to the deck. */
  function addCard(card) {
    const copies = inDeck[card.id] ?? 0
    if (copies >= copyLimit(card) || totalCards >= DECK_RULES.cards) return
    setInDeck({ ...inDeck, [card.id]: copies + 1 })
  }

  /** Removes one copy of a card from the deck. */
  function removeCard(idOfCard) {
    const { [idOfCard]: copies, ...others } = inDeck
    setInDeck(copies > 1 ? { ...inDeck, [idOfCard]: copies - 1 } : others)
  }

  /** Saves the deck (new or edited) and goes back to the deck list. */
  async function save() {
    setSaving({ inProgress: true, error: null })
    try {
      await saveDeck(
        { name: name.trim(), echoes, cardList: rows.map(({ card, copies }) => ({ cardId: card.id, copies })) },
        id,
      )
      navigate('/collection/decks')
    } catch {
      setSaving({ inProgress: false, error: 'Salvataggio non riuscito. Riprova.' })
    }
  }

  const title = id ? initialName : 'Nuovo mazzo'
  const visibleCards = cardList ? filterCards(cardList, filters) : []
  const filterCount = countFilters(filters)
  const closeFilters = useCallback(() => setFiltersOpen(false), [])
  const closeName = useCallback(() => setNameOpen(false), [])

  return (
    <AppLayout
      activePage="collection"
      mobileBackground="/images/deck-background.png"
      desktopBackground="/images/deck-background-desktop.png"
      hideMobileMenu
    >
      <div className="-mx-5 -mt-8 lg:m-0 lg:flex lg:flex-col lg:gap-6">
        {/* 📱 Mobile: deck panel fixed at the top */}
        <div className="sticky top-18 z-10 flex flex-col gap-2.5 border-b border-gold-border bg-background px-5 pt-4 pb-3 shadow-[0_6px_14px_rgba(33,38,43,0.12)] lg:hidden">
          <div className="flex items-center gap-2.5">
            <Link to="/collection/decks" className="flex min-h-11 items-center font-medium text-ink-secondary">
              ‹ Mazzi
            </Link>
            <h1 className="truncate pl-3 font-title text-[22px]">{title}</h1>
          </div>
          <Counters echoes={echoes} totalCards={totalCards} totalPoints={totalPoints} pointsOver={pointsOver} />
          <Tray cardList={tray} onRemove={removeCard} />
          {pointsOver > 0 && <PointsNotice />}
        </div>

        {/* 💻 Desktop: header */}
        <div className="hidden flex-col gap-1.5 lg:flex">
          <h1 className="font-title text-h2">{title}</h1>
          <p>
            {id ? 'Modifica le carte del mazzo.' : 'Passo 2 di 2'} · Aggiungi {DECK_RULES.cards} carte di {echoes.join(' e ')}.
          </p>
          <Link to="/collection/decks" className="self-start font-medium underline">
            ‹ Mazzi
          </Link>
        </div>

        <div className="flex flex-col gap-3.5 px-5 pt-4 pb-28 lg:flex-row lg:items-start lg:gap-6 lg:p-0">
          {/* Your collection (only the 2 Echoes) */}
          <section className="flex flex-col gap-3.5 bg-background/86 lg:flex-1 lg:gap-5 lg:rounded-2xl lg:border lg:border-gold lg:bg-background lg:p-6">
            <div className="flex items-center justify-between gap-3">
              <div className="flex flex-col gap-0.5 leading-[1.4] lg:flex-row lg:items-baseline lg:gap-3">
                <h2 className="font-semibold lg:font-title lg:text-h2 lg:font-normal">La tua collezione</h2>
                <p className="text-caption font-medium">Solo {echoes.join(' e ')}</p>
              </div>
              <Button variant="secondary" onClick={() => setFiltersOpen(true)} className="w-auto min-w-37 shrink-0 lg:min-w-50">
                ☰ Filtri{filterCount > 0 && ` (${filterCount})`}
              </Button>
            </div>

            {loading && (
              <div className="grid grid-cols-3 gap-3">
                {[1, 2, 3].map((n) => (
                  <Skeleton key={n} className="aspect-[107/150] rounded-lg" />
                ))}
              </div>
            )}
            {error && <ErrorMessage />}
            {cardList && visibleCards.length === 0 && <p>Nessuna carta corrisponde ai filtri.</p>}

            {visibleCards.length > 0 && (
              <ul className="grid grid-cols-3 gap-x-3 gap-y-3.5 lg:grid-cols-[repeat(auto-fill,minmax(88px,1fr))] lg:gap-y-4">
                {visibleCards.map((card) => {
                  const copies = inDeck[card.id] ?? 0
                  return (
                    <li key={card.id}>
                      <DeckCard
                        card={card}
                        copies={copies}
                        limit={copyLimit(card)}
                        deckFull={totalCards >= DECK_RULES.cards}
                        onAdd={() => addCard(card)}
                      />
                    </li>
                  )
                })}
              </ul>
            )}
          </section>

          {/* 💻 Desktop: "Il tuo mazzo" (your deck) on the right */}
          <aside className="sticky top-24 hidden w-75 shrink-0 flex-col gap-4 rounded-2xl border border-gold bg-background p-4 lg:flex">
            <h2 className="font-title text-h2">Il tuo mazzo</h2>
            <Bar label="Carte" value={totalCards} maximum={DECK_RULES.cards} text={`${totalCards}/${DECK_RULES.cards}`} />
            <Bar label="Costo totale" value={totalPoints} maximum={DECK_RULES.points} text={`${totalPoints}/${DECK_RULES.points} punti`} over={pointsOver > 0} />
            <p className="text-caption leading-[1.4]">
              Regole: solo i {DECK_RULES.echoes} Echi scelti · massimo {DECK_RULES.copies} copie per carta · costo massimo{' '}
              {DECK_RULES.points} punti
            </p>
            {/* With many cards the list scrolls inside, so "Salva" (save) stays visible */}
            <ul className="flex max-h-[38vh] flex-col gap-1.5 overflow-y-auto">
              {rows.map(({ card, copies }) => (
                <li key={card.id} className="flex items-center gap-2 rounded-xl bg-background-secondary py-1.5 pr-1.5 pl-2">
                  <span className="flex-1 truncate font-medium">{card.name}</span>
                  <span className="text-caption">
                    {card.points} {card.points === 1 ? 'punto' : 'punti'}
                  </span>
                  <span className="font-semibold">×{copies}</span>
                  <button
                    type="button"
                    onClick={() => removeCard(card.id)}
                    aria-label={`Togli una copia di ${card.name}`}
                    className="flex size-8 items-center justify-center rounded-full border border-ink hover:bg-background"
                  >
                    −
                  </button>
                </li>
              ))}
            </ul>
            <DeckStatus missing={missing} pointsOver={pointsOver} />
            <Button onClick={() => setNameOpen(true)} disabled={!valid} className="min-h-12 w-full">
              Salva mazzo
            </Button>
          </aside>
        </div>
      </div>

      {/* 📱 Mobile: bottom bar with the status and "Salva mazzo" */}
      <div className="fixed inset-x-0 bottom-0 z-10 flex h-24 items-center gap-3 border-t border-gold-border bg-background px-5 pb-5 lg:hidden">
        <p className={`text-caption leading-[1.4] font-medium ${pointsOver > 0 ? 'text-error' : ''}`} aria-live="polite">
          {pointsOver > 0
            ? `⚠ ${pointsOver} punti oltre il limite di ${DECK_RULES.points}`
            : missing > 0
              ? `Mancano ${missing} ${missing === 1 ? 'carta' : 'carte'}`
              : '✓ Mazzo completo'}
        </p>
        <Button onClick={() => setNameOpen(true)} disabled={!valid} className="flex-1 disabled:opacity-55">
          Salva mazzo
        </Button>
      </div>

      {filtersOpen && cardList && (
        <CardFiltersModal
          cardList={cardList}
          filters={filters}
          onClose={closeFilters}
          onApply={(newOnes) => {
            setFilters(newOnes)
            setFiltersOpen(false)
          }}
        />
      )}

      {/* 5s · Give the deck a name */}
      {nameOpen && (
        <Modal title="Dai un nome al mazzo" onClose={closeName}>
          <p className="text-ink-secondary">
            Il mazzo è completo: {totalCards} carte, {totalPoints}/{DECK_RULES.points} punti, {echoes.join(' + ')}.
          </p>
          <label className="flex flex-col gap-2 leading-[1.4] font-semibold">
            Nome del mazzo
            <input
              type="text"
              value={name}
              onChange={(event) => setName(event.target.value)}
              maxLength={DECK_RULES.nameLength}
              className="h-12 rounded-xl border-[1.5px] border-ink bg-background px-4 font-normal"
            />
          </label>
          <p className="text-caption leading-[1.4] font-medium">
            Massimo {DECK_RULES.nameLength} caratteri · puoi cambiarlo dopo
          </p>
          <Button onClick={save} disabled={!name.trim() || saving.inProgress}>
            {saving.inProgress ? 'Salvataggio…' : 'Salva mazzo'}
          </Button>
          <Button variant="secondary" onClick={closeName}>
            Torna al mazzo
          </Button>
          {saving.error && <ErrorMessage text={saving.error} />}
        </Modal>
      )}
    </AppLayout>
  )
}

/**
 * Pills at the top: cards, points and the 2 Echoes
 *
 * @param {object} props - the component props
 * @param {string[]} props.echoes
 * @param {number} props.totalCards
 * @param {number} props.totalPoints
 * @param {number} props.pointsOver
 * @returns {JSX.Element}
 */
function Counters({ echoes, totalCards, totalPoints, pointsOver }) {
  const pill = 'rounded-full border-[1.2px] px-2.5 py-1 text-caption leading-[1.4] font-medium'
  return (
    <div className="flex flex-wrap gap-1.5">
      <span className={`${pill} border-ink bg-background-secondary`}>
        {totalCards}/{DECK_RULES.cards} carte
      </span>
      <span className={`${pill} ${pointsOver > 0 ? 'border-error bg-error-background text-error' : 'border-ink bg-background-secondary'}`}>
        {totalPoints}/{DECK_RULES.points} punti
      </span>
      {echoes.map((name) => {
        const echo = findEcho(name)
        return (
          <span key={name} className={`${pill} bg-background ${echo?.border} ${echo?.text}`}>
            {name}
          </span>
        )
      })}
    </div>
  )
}

/**
 * The "tray": 30 boxes on 2 rows. Tap a card to remove it.
 *
 * @param {object} props - the component props
 * @param {object[]} props.cardList
 * @param {Function} props.onRemove
 * @returns {JSX.Element}
 */
function Tray({ cardList, onRemove }) {
  const boxes = Array.from({ length: DECK_RULES.cards }, (_, i) => cardList[i])
  return (
    <div className="h-36.5 overflow-x-auto rounded-xl bg-[rgba(33,38,43,0.06)] p-2">
      <ul className="grid w-max grid-cols-[repeat(15,44px)] grid-rows-2 gap-1.5">
        {boxes.map((card, i) => (
          <li key={i}>
            {card ? (
              <button type="button" onClick={() => onRemove(card.id)} aria-label={`Togli ${card.name}`} className="block">
                <CardImage card={card} className="h-15.5 w-11" />
              </button>
            ) : (
              <span className="block h-15.5 w-11 rounded-[5px] border-[1.2px] border-dashed border-gold-border bg-background/60" />
            )}
          </li>
        ))}
      </ul>
    </div>
  )
}

/**
 * A collection card: tap it to add it to the deck
 *
 * @param {object} props - the component props
 * @param {object} props.card
 * @param {number} props.copies - copies already in the deck
 * @param {number} props.limit - copies you can still add
 * @param {boolean} props.deckFull
 * @param {Function} props.onAdd
 * @returns {JSX.Element}
 */
function DeckCard({ card, copies, limit, deckFull, onAdd }) {
  const soldOut = copies >= limit
  const echo = findEcho(card.echo)
  let desktopStatus = 'Non nel mazzo'
  if (copies > 0) desktopStatus = copies >= card.amount ? `Nel mazzo ${copies} · tutte le tue copie` : `Nel mazzo ${copies}/${limit}`

  return (
    <button
      type="button"
      onClick={onAdd}
      disabled={soldOut || deckFull}
      aria-label={`Aggiungi ${card.name}`}
      className="flex w-full flex-col items-start gap-1 text-left text-caption leading-[1.4] font-medium disabled:cursor-not-allowed"
    >
      <span className={`relative block aspect-[107/150] w-full ${soldOut ? 'lg:opacity-50' : ''}`}>
        <CardImage card={card} className="size-full" />
        <span className="absolute -top-1.5 right-1 rounded-full border-[1.5px] border-background bg-ink px-1.75 py-0.5 text-background">
          ×{card.amount}
        </span>
        {copies > 0 && (
          <span className="absolute bottom-1 left-1 rounded-full border border-ink bg-gold px-1.75 py-0.5 lg:hidden">
            Nel mazzo {copies}
          </span>
        )}
      </span>
      <span className="lg:font-semibold">{card.name}</span>
      <span className={`${echo?.text} lg:text-ink`}>
        {card.points} {card.points === 1 ? 'punto' : 'punti'} · {card.echo}
      </span>
      <span className="hidden lg:block">{desktopStatus}</span>
    </button>
  )
}

/**
 * Progress bar (desktop): Cards and Total cost
 *
 * @param {object} props - the component props
 * @param {string} props.label
 * @param {number} props.value
 * @param {number} props.maximum
 * @param {string} props.text
 * @param {boolean} [props.over=false]
 * @returns {JSX.Element}
 */
function Bar({ label, value, maximum, text, over = false }) {
  return (
    <div className="flex flex-col gap-1.5 leading-[1.4]">
      <div className="flex justify-between font-semibold">
        <span>{label}</span>
        <span className={over ? 'text-error' : ''}>{text}</span>
      </div>
      <div className="h-2 overflow-hidden rounded border border-ink-secondary bg-background-secondary">
        <div className={`h-full ${over ? 'bg-error' : 'bg-gold-border'}`} style={{ width: `${Math.min(100, (value / maximum) * 100)}%` }} />
      </div>
    </div>
  )
}

/**
 * Warning when the deck goes over the maximum points.
 *
 * @returns {JSX.Element}
 */
function PointsNotice() {
  return (
    <p role="alert" className="flex gap-2.5 rounded-xl border border-error bg-error-background px-3.5 py-3 text-caption leading-[1.4] font-medium">
      <span aria-hidden="true" className="text-error">⚠</span>
      Hai superato il costo massimo. Togli una carta o scegline una che costa meno.
    </p>
  )
}

/**
 * Message under the list (desktop)
 *
 * @param {object} props - the component props
 * @param {number} props.missing
 * @param {number} props.pointsOver
 * @returns {JSX.Element}
 */
function DeckStatus({ missing, pointsOver }) {
  if (pointsOver > 0) return <PointsNotice />
  if (missing > 0) {
    return (
      <p className="rounded-xl border border-info bg-info-background px-3.5 py-3 text-caption leading-[1.4] font-medium">
        Mancano {missing} {missing === 1 ? 'carta' : 'carte'} per completare il mazzo.
      </p>
    )
  }
  return (
    <p className="rounded-xl border border-success bg-success-background px-3.5 py-3 text-caption leading-[1.4] font-medium">
      ✓ Mazzo completo: puoi salvarlo.
    </p>
  )
}
