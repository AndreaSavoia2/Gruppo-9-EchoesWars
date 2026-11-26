import { useEffect, useRef, useState } from 'react'
import CardImage from '../card/CardImage'
import './pack-opening.css'

// How long the opening phases last (milliseconds)
const SHAKE_MS = 420 // the pack shakes
const TEAR_MS = 600 // the strip at the top flies away

/**
 * true if the user asked the system to reduce animations
 * @returns {boolean}
 */
const reducedMotion = () => window.matchMedia?.('(prefers-reduced-motion: reduce)').matches ?? false

/**
 * Pack opening, full screen.
 *   1. closed pack on the magic circle → it opens with a CLICK (or Enter),
 *      or by dragging along the top edge to tear it
 *   2. the pack shakes, the strip flies away, rays of light come out
 *   3. the cards come out in a fan, face down: the back glows if it is Rare (blue) or Legendary (gold)
 *   4. one card is flipped at a time (or "Gira tutte"); the legendary one has sparkles and an announcement
 *   5. when they are all flipped: summary and "Vedi le carte"
 * The style is in pack-opening.css (plain CSS, "opening-" classes).
 *
 * @param {object} props - the component props
 * @param {object[]} props.cardList - the cards found (rarity: "Comune" | "Rara" | "Leggendaria"; isNew: true if you did not have it)
 * @param {string} props.summary - sentence like "3 comuni, 1 rara e 1 leggendaria."
 * @param {Function} props.onDone - called by the "Vedi le carte" button
 * @returns {JSX.Element}
 */
export default function PackOpening({ cardList, summary, onDone }) {
  const [phase, setPhase] = useState('closed') // 'closed' → 'opening' → 'cards'
  const [open, setOpen] = useState(false) // the strip at the top is flying away
  const [flippedSet, setFlippedSet] = useState(() => new Set())
  const [announcement, setAnnouncement] = useState('')
  const [shaking, setShaking] = useState(false) // true for a moment: the stage shakes
  const [tear, setTear] = useState(0) // 0…1 how much it is torn
  const [hint, setHint] = useState('Clicca la busta per aprirla, oppure trascina sul bordo in alto per strapparla')
  const dragRef = useRef(null)
  const packRef = useRef(null)
  const handRef = useRef(null)

  const allFlipped = flippedSet.size === cardList.length
  const fan = cardList.length <= 5

  // At the start the keyboard focus goes on the pack; when the cards come out, on the first card
  useEffect(() => {
    if (phase === 'closed') packRef.current?.focus()
    if (phase === 'cards') handRef.current?.querySelector('button')?.focus()
  }, [phase])

  /** Opens the pack: shake → strip flying away + light → cards. */
  function openPack() {
    if (phase !== 'closed') return
    setPhase('opening')
    setHint('')
    const reduced = reducedMotion()
    setTimeout(() => setOpen(true), reduced ? 0 : SHAKE_MS)
    setTimeout(() => setPhase('cards'), reduced ? 150 : SHAKE_MS + TEAR_MS)
  }

  // ---- Tearing with the finger or the mouse ----
  // A click (moves less than 6 px) opens straight away; a drag along the top edge tears it.
  /** Pointer pressed on the pack: remembers where the drag started. */
  function onDown(event) {
    if (phase !== 'closed') return
    const r = event.currentTarget.getBoundingClientRect()
    dragRef.current = {
      x0: event.clientX,
      y0: event.clientY,
      width: r.width,
      atTop: event.clientY - r.top < r.height * 0.35,
      moved: false,
    }
    event.currentTarget.setPointerCapture(event.pointerId)
  }
  /** Pointer moving: along the top edge it tears the pack. */
  function onMove(event) {
    const t = dragRef.current
    if (!t) return
    if (Math.abs(event.clientX - t.x0) + Math.abs(event.clientY - t.y0) > 6) t.moved = true
    if (!t.atTop || !t.moved) return
    const p = Math.min(Math.max((event.clientX - t.x0) / (t.width * 0.8), 0), 1)
    setTear(p)
    if (p >= 1) {
      dragRef.current = null
      openPack()
    }
  }
  /** Pointer released: a click opens the pack, a short drag shows a hint. */
  function onUp() {
    const t = dragRef.current
    if (!t) return
    dragRef.current = null
    if (!t.moved) return openPack() // it was a click
    setTear(0)
    setHint('Ancora un po’: trascina fino in fondo, oppure clicca la busta')
  }

  /**
   * Flips a card. If it is legendary: announcement, sparkles and shaking.
   * @param {number} i - position of the card
   * @returns {void}
   */
  function flip(i) {
    if (flippedSet.has(i)) return
    setFlippedSet((before) => new Set(before).add(i))
    if (cardList[i].rarity === 'Leggendaria') {
      setAnnouncement('Leggendaria!')
      setShaking(true)
      setTimeout(() => setShaking(false), 400)
    }
  }

  /** Flips the remaining cards, one after the other. */
  function flipAll() {
    cardList.forEach((_, i) => {
      if (!flippedSet.has(i)) setTimeout(() => flip(i), i * 220)
    })
  }

  return (
    <div className="opening-screen" role="dialog" aria-modal="true" aria-label="Apertura busta">
      <div className={`opening-stage ${shaking ? 'opening-tremble' : ''}`}>
        <div className={`opening-rays ${open ? 'opening-lit' : ''}`} />

        <p className="opening-announcement" aria-live="polite">
          {announcement && <span className="opening-pill">{announcement}</span>}
        </p>

        {phase !== 'cards' ? (
          <button
            ref={packRef}
            type="button"
            aria-label="Apri la busta"
            className={`opening-pack ${tear > 0 ? 'opening-pulling' : ''} ${phase === 'opening' && !open ? 'opening-shake' : ''} ${open ? 'opening-opened' : ''}`}
            style={tear > 0 && phase === 'closed' ? { transform: `rotate(${tear * 2}deg)` } : undefined}
            onPointerDown={onDown}
            onPointerMove={onMove}
            onPointerUp={onUp}
            onKeyDown={(event) => {
              if (event.key === 'Enter' || event.key === ' ') {
                event.preventDefault()
                openPack()
              }
            }}
          >
            <img className="opening-pack-bottom" src="/images/pack.png" alt="" />
            <img className="opening-pack-top" src="/images/pack.png" alt="" />
            {phase === 'closed' && (
              <span className="opening-tear" aria-hidden="true">
                <i style={{ width: `${tear * 100}%` }} />
              </span>
            )}
          </button>
        ) : (
          <ul ref={handRef} className={`opening-hand ${fan ? 'opening-fan' : ''}`} aria-label="Carte trovate">
            {cardList.map((card, i) => {
              const pos = i - (cardList.length - 1) / 2 // -2 … 2 with 5 cards
              const flipped = flippedSet.has(i)
              const rarity = card.rarity === 'Rara' ? 'opening-rare' : card.rarity === 'Leggendaria' ? 'opening-legendary' : ''
              return (
                <li key={`${card.id ?? 'c'}-${i}`} className="contents">
                  <button
                    type="button"
                    onClick={() => flip(i)}
                    aria-label={flipped ? `${card.name}, carta ${card.rarity}${card.isNew ? ', nuova' : ''}` : `Carta ${i + 1} coperta: gira`}
                    className={`opening-card ${rarity} ${flipped ? 'opening-flipped' : ''}`}
                    style={{ '--opening-i': i, '--opening-pos': pos, '--opening-y': `${Math.abs(pos) * 10}px` }}
                  >
                    <span className="opening-faces">
                      <span className="opening-face opening-back">
                        <img src="/icons/star.png" alt="" />
                      </span>
                      <span className="opening-face opening-front">
                        <CardImage card={card} className="size-full" />
                        {card.isNew && <span className="opening-new">Nuova!</span>}
                        {flipped && card.rarity === 'Leggendaria' && <Sparkles />}
                      </span>
                    </span>
                  </button>
                </li>
              )
            })}
          </ul>
        )}

        {/* Hints and controls at the bottom */}
        <div className="opening-controls" aria-live="polite">
          {phase === 'closed' && hint && <p className="opening-pill">{hint}</p>}
          {phase === 'cards' && !allFlipped && (
            <>
              <p className="opening-pill">Tocca una carta per girarla</p>
              <button type="button" className="opening-button" onClick={flipAll}>
                Gira tutte
              </button>
            </>
          )}
          {phase === 'cards' && allFlipped && (
            <>
              {summary && <p className="opening-pill">{summary}</p>}
              <button type="button" className="opening-button opening-primary" onClick={onDone}>
                Vedi le carte
              </button>
            </>
          )}
        </div>
      </div>
    </div>
  )
}

/**
 * Golden sparkles flying away from the legendary card (18, in a circle).
 * @returns {JSX.Element}
 */
function Sparkles() {
  return (
    <span aria-hidden="true">
      {Array.from({ length: 18 }, (_, k) => {
        const angle = (k / 18) * Math.PI * 2
        const distance = 70 + (k % 3) * 25
        return (
          <span
            key={k}
            className="opening-sparkle"
            style={{ '--opening-dx': `${Math.cos(angle) * distance}px`, '--opening-dy': `${Math.sin(angle) * distance}px` }}
          />
        )
      })}
    </span>
  )
}
