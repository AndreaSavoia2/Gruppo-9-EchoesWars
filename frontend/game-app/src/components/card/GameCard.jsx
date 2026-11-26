import { useLayoutEffect, useRef } from 'react'
import { FRAMES, normalizeCard, pickFrame } from '../../data/cards'

/**
 * 🃏 A game card, built in LAYERS (as in the team's card.html file):
 *
 *   layer 1 (bottom): the illustration      → changes for every card
 *   layer 2:          the frame (image)     → one per Echo + rarity (+ school)
 *   layer 3 (top):    the texts             → cost, name, school, echo, description, ATK, DEF
 *
 * Why layers and not a single image? With 100 cards only a few frames are needed
 * + 100 illustrations, and the texts (name, values) come from the backend: if a
 * number changes there is no need to redraw the image.
 *
 * The card adapts to ANY width: all the sizes are in "card points"
 * (the card is 750 × 1050 points) and 1 point = card width / 750.
 * Just give the width with className, e.g. <GameCard card={...} className="w-40" />
 *
 * Expected data (⚠️ TO BE CONFIRMED with the backend):
 * {
 *   "code": "DOM01", "name": "Cadetto",
 *   "atk": 5, "def": 4, "cost": 3,
 *   "echo": "DOMINION", "school": "WARRIOR", "rarity": "COMMON",   // Italian is fine too
 *   "effect": "PIERCING",          // or null
 *   "description": "Giovane recluta…",   // used when there is no effect
 *   "illustration": "https://…/illustrations/DOM01.webp",  // or null (shows a placeholder)
 *   "frame": "https://…/cornici/dominio_comune.webp"      // the file NAME decides the zones
 * }
 * def: if the DEF dropped in battle, pass the current DEF (e.g. { ...card, def: 1 }).
 *
 * @param {object} props - the component props
 * @param {object} props.card
 * @param {string} [props.className='w-full']
 * @returns {JSX.Element}
 */
export default function GameCard({ card: rawCard, className = 'w-full' }) {
  const card = normalizeCard(rawCard)
  const frame = pickFrame(card) // { name, src }
  const z = FRAMES[frame.name]
  const rootEl = useRef(null)

  // Position calculations (as in the team's file)
  const bandY = z.artBottom + (z.text[1] - z.artBottom) * 0.42 // "School · Echo" band
  const statsY = z.text[3] + 8 // ATK and DEF at the bottom

  // Long texts shrink until they fit in their space
  useLayoutEffect(() => {
    const el = rootEl.current
    if (!el) return
    const fit = () => shrinkTexts(el)
    fit()
    document.fonts?.ready.then(fit) // recalculate when the fonts are loaded
    // ...and when the card changes size (e.g. it was hidden and now it is visible)
    const observer = new ResizeObserver(fit)
    observer.observe(el)
    return () => observer.disconnect()
  }, [card.name, card.description, card.effect?.name, frame.name])

  return (
    <article
      ref={rootEl}
      aria-label={screenReaderText(card)}
      className={`relative aspect-[750/1050] shrink-0 font-['EB_Garamond',serif] text-[#3a2916] select-none [container-type:inline-size] ${className}`}
      style={{ '--u': 'calc(100cqw / 750)' }}
    >
      {/* 1. Illustration (or placeholder) */}
      {card.illustration ? (
        <img src={card.illustration} alt="" className="absolute object-cover" style={zone(z.art[0], z.art[1] - 6, z.art[2], z.artBottom + 6)} />
      ) : (
        <div
          className="absolute grid place-items-center bg-[radial-gradient(circle_at_50%_35%,#7d6a58,#2b241d_80%)] font-['Cormorant_Garamond',serif] font-bold tracking-[0.2em] text-[#f4e8d0]/40"
          style={{ ...zone(z.art[0], z.art[1] - 6, z.art[2], z.artBottom + 6), fontSize: u(26) }}
        >
          ILLUSTRAZIONE
        </div>
      )}

      {/* 2. Frame */}
      <img src={frame.src} alt="" className="pointer-events-none absolute inset-0 size-full" />

      {/* 3. Texts. aria-hidden: the screen reader already reads the aria-label of the card */}
      <div aria-hidden="true">
        <StatValue style={zone(56, 50, 136, 130)}>{card.cost}</StatValue>

        <div
          className="absolute flex items-center justify-center text-center leading-[1.05] font-['Cormorant_Garamond',serif] font-bold"
          style={{ ...zone(z.name[0] + 10, z.name[1], z.name[2] - 10, z.name[3]), fontSize: u(40) }}
        >
          {card.name}
        </div>

        <Band style={zone(190, bandY - 25, 392, bandY + 25)}>Scuola: {card.school}</Band>
        <Band style={zone(515, bandY - 25, 695, bandY + 25)}>Eco: {card.echo}</Band>

        {/* Parchment: effect (if any) or description in italics */}
        <div
          data-fit={card.effect ? 25 : 29}
          className="absolute flex items-center justify-center overflow-hidden"
          style={zone(z.text[0] + 26, z.text[1] + 16, z.text[2] - 26, z.text[3] - 64)}
        >
          <div className="w-full text-center leading-[1.18]">
            {card.effect ? (
              <>
                <b className="mb-[0.25em] block font-bold">Effetto: {card.effect.name}</b>
                <p>{card.effect.text}</p>
              </>
            ) : (
              <i className="font-medium">{card.description}</i>
            )}
          </div>
        </div>

        <StatValue style={zone(135, statsY - 45, 235, statsY + 45)}>{card.atk}</StatValue>
        <StatValue style={zone(515, statsY - 45, 615, statsY + 45)}>{card.def}</StatValue>
      </div>
    </article>
  )
}

/**
 * The sentence the screen reader reads for the card, all together and in order.
 * Example: "Cadetto, carta Comune. Eco Dominio, scuola Guerriero. Costo 3, ATK 5, DEF 4.
 *           Nessun effetto. Giovane recluta del Dominio…"
 * (the sentence is in Italian because it is read to the user)
 * @param {object} card - card already normalized (names in Italian)
 * @returns {string}
 */
export function screenReaderText(card) {
  const parts = [
    `${card.name}${card.rarity ? `, carta ${card.rarity}` : ''}.`,
    [card.echo && `Eco ${card.echo}`, card.school && `scuola ${card.school}`].filter(Boolean).join(', ') + '.',
    `Costo ${card.cost}, ATK ${card.atk}, DEF ${card.def}.`,
    card.effect ? `Effetto ${card.effect.name}: ${card.effect.text}` : 'Nessun effetto.',
    !card.effect && card.description ? card.description : '',
  ]
  // Removes the empty parts (e.g. ". " if Echo and school are missing)
  return parts.filter((part) => part && part !== '.').join(' ')
}

/**
 * n card points → CSS size
 *
 * @param {number} n
 * @returns {string}
 */
const u = (n) => `calc(${n} * var(--u))`

/**
 * Rectangle from (x0, y0) to (x1, y1) in card points → CSS style
 *
 * @param {number} x0
 * @param {number} y0
 * @param {number} x1
 * @param {number} y1
 * @returns {object}
 */
const zone = (x0, y0, x1, y1) => ({ left: u(x0), top: u(y0), width: u(x1 - x0), height: u(y1 - y0) })

/**
 * Big white numbers: cost, ATK, DEF
 *
 * @param {object} props - the component props
 * @param {object} props.style
 * @param {React.ReactNode} props.children
 * @returns {JSX.Element}
 */
function StatValue({ style, children }) {
  return (
    <div
      className="absolute flex items-center justify-center font-['Playfair_Display',serif] leading-none font-black text-white tabular-nums"
      style={{ ...style, fontSize: u(88), textShadow: `0 ${u(3)} ${u(6)} #000a` }}
    >
      {children}
    </div>
  )
}

/**
 * Texts in the coloured band: "Scuola: …" and "Eco: …"
 *
 * @param {object} props - the component props
 * @param {object} props.style
 * @param {React.ReactNode} props.children
 * @returns {JSX.Element}
 */
function Band({ style, children }) {
  return (
    <div
      data-band
      className="absolute flex items-center justify-start overflow-hidden leading-none font-bold whitespace-nowrap text-[#f3e5c2]"
      style={{ ...style, fontSize: u(26) }}
    >
      {children}
    </div>
  )
}

/**
 * Shrinks the text one point at a time until it no longer overflows its space
 *
 * @param {HTMLElement} card - the card element that contains the texts
 * @returns {void}
 */
function shrinkTexts(card) {
  if (!card.clientWidth) return // card hidden: we will try again when it is visible
  card.querySelectorAll('[data-fit]').forEach((el) => {
    let size = Number(el.dataset.fit)
    do {
      el.style.fontSize = u(size--)
    } while (el.firstChild.offsetHeight > el.clientHeight && size > 14)
  })
  card.querySelectorAll('[data-band]').forEach((el) => {
    let size = 26
    do {
      el.style.fontSize = u(size--)
    } while (el.scrollWidth > el.clientWidth && size > 14)
  })
}
