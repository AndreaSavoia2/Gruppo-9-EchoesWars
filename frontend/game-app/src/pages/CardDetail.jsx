import { useCallback, useState } from 'react'
import { Link, useParams } from 'react-router'
import AppLayout from '../components/AppLayout'
import Button, { LinkButton } from '../components/Button'
import Modal from '../components/Modal'
import EnlargedCard from '../components/collection/EnlargedCard'
import Skeleton from '../components/Skeleton'
import ErrorMessage from '../components/ErrorMessage'
import { useFetch } from '../hooks/useFetch'
import { useUser } from '../context/UserContext'
import { convertCopies, getCard } from '../api/collection'
import { rarityBorder } from '../data/rarity'
import { normalizeCard } from '../data/cards'
import CardImage from '../components/card/CardImage'

// Label colour for every Echo ("factions" colours of the Figma)
const echoColor = {
  Dominio: 'border-dominion text-dominion',
  Sapienza: 'border-wisdom text-wisdom',
  Inganno: 'border-deception text-deception',
  Oblio: 'border-oblivion text-oblivion',
}

/**
 * 6 · 6c · 6d · 6e / D6 · D6b · D6c · /collection/card/:id
 *
 * @returns {JSX.Element}
 */
export default function CardDetail() {
  const { id } = useParams()
  const loadCard = useCallback((signal) => getCard(id, signal), [id])
  const { data, loading, error, reload } = useFetch(loadCard)
  // Translates the backend codes into Italian (e.g. "DOMINION" → "Dominio", "COMMON" → "Comune")
  const card = data && normalizeCard(data)
  const { reload: reloadUser } = useUser()

  const [confirmOpen, setConfirmOpen] = useState(false)
  const [conversion, setConversion] = useState({ inProgress: false, outcome: null, error: null })
  const closeConfirm = useCallback(() => setConfirmOpen(false), [])
  // 6e: enlarged card open yes/no
  const [enlarged, setEnlarged] = useState(false)
  const closeEnlarged = useCallback(() => setEnlarged(false), [])

  /** Converts the duplicates of this card into credits. */
  async function convert() {
    setConversion({ inProgress: true, outcome: null, error: null })
    try {
      const outcome = await convertCopies(id)
      setConversion({ inProgress: false, outcome, error: null })
      setConfirmOpen(false)
      reload() // refreshes the card copies
      reloadUser() // refreshes the credits at the top
    } catch {
      setConversion({ inProgress: false, outcome: null, error: 'Conversione non riuscita. Riprova.' })
    }
  }

  return (
    <AppLayout activePage="collection" mobileBackground="/images/collection-background.png" desktopBackground="/images/collection-background.png">
      <div className="-mt-6 flex flex-col gap-3 lg:mt-0 lg:gap-6">
        <Link to="/collection" className="flex min-h-11 items-center self-start font-medium lg:min-h-0 lg:underline">
          ‹ Collezione
        </Link>

        {loading && <Skeleton className="mx-auto h-94.75 w-67.5 rounded-lg lg:mx-0 lg:h-133.25 lg:w-95" />}
        {error && <ErrorMessage text="Impossibile caricare la carta. Riprova più tardi." />}

        {card && (
          <div className="flex flex-col items-center gap-3 lg:flex-row lg:items-start lg:gap-10">
            {/* Big card with the copies: it is a button, tapping it opens the enlarged version (6e) */}
            <div className="flex shrink-0 flex-col items-center gap-3">
              <button
                type="button"
                onClick={() => setEnlarged(true)}
                aria-label={`Ingrandisci la carta ${card.name}`}
                className="relative h-94.75 w-67.5 cursor-zoom-in lg:h-133.25 lg:w-95"
              >
                <CardImage card={card} className="size-full lg:rounded-3xl" />
                <span className="absolute -top-1.5 right-1 rounded-full border-[1.5px] border-background bg-ink px-2 py-0.5 text-caption leading-[1.4] font-medium text-background">
                  ×{card.amount}
                  <span className="sr-only"> copie</span>
                </span>
              </button>
              {/* aria-hidden: for the screen reader the button label is enough */}
              <p aria-hidden="true" className="text-caption leading-[1.4] font-medium text-ink-secondary">
                🔍 <span className="lg:hidden">Tocca</span>
                <span className="hidden lg:inline">Clicca</span> la carta per vederla in grande
              </p>
            </div>

            <div className="flex w-full flex-col items-center gap-3 lg:flex-1 lg:items-start lg:gap-5">
              <h1 className="text-center font-title text-[22px] lg:text-h2">{card.name}</h1>

              {/* Labels: coloured on mobile, neutral with "Eco:" and "Scuola:" on desktop */}
              <ul className="flex flex-wrap justify-center gap-2">
                <Tag mobile={card.echo} desktop={`Eco: ${card.echo}`} color={echoColor[card.echo]} />
                <Tag mobile={card.school} desktop={`Scuola: ${card.school}`} color="border-ink text-ink" />
                <Tag mobile={card.rarity} desktop={`◆ ${card.rarity}`} color={rarityBorder[card.rarity]} />
              </ul>

              <Stats card={card} />

              {/* 💻 Desktop: abilities and copies */}
              <section className="hidden w-full flex-col gap-1.5 rounded-2xl border border-gold bg-background p-6 lg:flex">
                <h2 className="leading-[1.4] font-semibold">Abilità: {card.effect?.name ?? card.ability ?? "Nessuna"}</h2>
                <p>
                  Ne possiedi {card.amount} copie · {card.copiesInDecks} sono nei tuoi mazzi attivi.
                </p>
              </section>

              {/* 6d · Conversion succeeded */}
              {conversion.outcome && (
                <p role="status" className="flex w-full gap-2.5 rounded-xl border border-success bg-success-background px-3.5 py-3 text-caption leading-[1.4] font-medium">
                  <span aria-hidden="true" className="text-success">✓</span>
                  +{conversion.outcome.creditsAdded} crediti aggiunti. Ti restano {conversion.outcome.amount} copie di
                  questa carta.
                </p>
              )}

              <div className="flex w-full flex-col gap-2.5 lg:flex-row lg:gap-3">
                <LinkButton to="/collection/decks" className="w-full lg:min-h-12 lg:w-auto lg:min-w-50">
                  Aggiungi a un mazzo
                </LinkButton>
                <LinkButton to="/trades" variant="secondary" className="w-full lg:min-h-12 lg:w-auto lg:min-w-50">
                  Proponi uno scambio
                </LinkButton>
                {/* 📱 Mobile: red button to convert */}
                {card.conversion && (
                  <Button variant="destructive" onClick={() => setConfirmOpen(true)} className="w-full lg:hidden">
                    Converti {card.conversion.copies} copie in crediti
                  </Button>
                )}
              </div>

              {/* 💻 Desktop: "Converti i doppioni" (convert duplicates) box */}
              {card.conversion && (
                <section className="hidden w-full flex-col items-start gap-2.5 rounded-2xl border border-gold bg-background p-6 lg:flex">
                  <h2 className="font-title text-h2">Converti i doppioni in crediti</h2>
                  <p>
                    Puoi convertire {card.conversion.copies} copie identiche di questa carta. Per una carta{' '}
                    {card.rarity} ricevi {card.conversion.credits} crediti.
                  </p>
                  <Notice />
                  <Button variant="destructive" onClick={() => setConfirmOpen(true)} className="min-h-12 w-85">
                    Converti {card.conversion.copies} copie in {card.conversion.credits} crediti
                  </Button>
                </section>
              )}
            </div>
          </div>
        )}
      </div>

      {/* 6c / D6b · Confirmation before deleting the copies */}
      {enlarged && card && <EnlargedCard card={card} onClose={closeEnlarged} />}
      {confirmOpen && card?.conversion && (
        <Modal title={`Convertire ${card.conversion.copies} copie?`} onClose={closeConfirm}>
          <p className="text-ink-secondary">
            Riceverai {card.conversion.credits} crediti (carta {card.rarity}). Le {card.conversion.copies} copie di
            “{card.name}” verranno eliminate per sempre. Ti resteranno {card.amount - card.conversion.copies} copie.
          </p>
          <Button variant="destructive" onClick={convert} disabled={conversion.inProgress}>
            {conversion.inProgress ? 'Conversione…' : `Converti e ricevi ${card.conversion.credits} crediti`}
          </Button>
          <Button variant="secondary" onClick={closeConfirm}>
            Annulla
          </Button>
          {conversion.error && <ErrorMessage text={conversion.error} />}
        </Modal>
      )}
    </AppLayout>
  )
}

/**
 * Card tag (Echo, School, Rarity): different text on mobile and desktop.
 *
 * @param {object} props - the component props
 * @param {string} props.mobile - text on mobile
 * @param {string} props.desktop - text on desktop
 * @param {string} props.color - Tailwind colour classes
 * @returns {JSX.Element}
 */
function Tag({ mobile, desktop, color }) {
  return (
    <li>
      <span className={`block rounded-full border-[1.5px] bg-background px-3 py-1 text-caption leading-[1.4] font-medium lg:hidden ${color}`}>
        {mobile}
      </span>
      <span className="hidden rounded-full border border-gold bg-background-secondary px-3.5 py-2 leading-[1.4] font-semibold lg:block">
        {desktop}
      </span>
    </li>
  )
}

/**
 * ATK · DEF · Cost (+ Effect only on mobile)
 *
 * @param {object} props - the component props
 * @param {object} props.card
 * @returns {JSX.Element}
 */
function Stats({ card }) {
  const values = [
    ['ATK', card.atk],
    ['DEF', card.def],
    ['Costo', card.cost],
  ]
  return (
    <dl className="flex w-full gap-2.5 lg:gap-3">
      {values.map(([name, value]) => (
        <div
          key={name}
          className="flex flex-1 flex-col-reverse items-center gap-0.5 rounded-xl bg-background-secondary py-2 leading-[1.4] lg:rounded-2xl lg:border lg:border-gold lg:px-4.5 lg:py-3.5"
        >
          <dt className="text-caption font-medium text-ink-secondary lg:text-ink">{name}</dt>
          <dd className="font-semibold lg:font-title lg:text-[36px] lg:leading-normal lg:font-normal">{value}</dd>
        </div>
      ))}
      <div className="flex flex-1 flex-col-reverse items-center gap-0.5 rounded-xl bg-background-secondary py-2 leading-[1.4] lg:hidden">
        <dt className="text-caption font-medium text-ink-secondary">Effetto</dt>
        <dd className="font-semibold">{card.effect?.name ?? card.ability ?? "Nessuna"}</dd>
      </div>
    </dl>
  )
}

/**
 * Red warning: the converted copies are deleted for ever.
 *
 * @returns {JSX.Element}
 */
function Notice() {
  return (
    <p className="flex w-full gap-2.5 rounded-xl border border-error bg-error-background px-3.5 py-3 text-caption leading-[1.4] font-medium">
      <span aria-hidden="true" className="text-body font-semibold text-error">⚠</span>
      Le copie convertite vengono eliminate per sempre. Le carte usate nei mazzi attivi non vengono convertite.
    </p>
  )
}
