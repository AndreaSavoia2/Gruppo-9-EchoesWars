import { useLayoutEffect, useRef } from 'react'
import { CORNICI, normalizzaCarta, sceltaCornice } from '../../data/carte'

// 🃏 Una carta del gioco, costruita a STRATI (come nel file card.html del team):
//
//   strato 1 (sotto): l'illustrazione        → cambia per ogni carta
//   strato 2:         la cornice (immagine)  → una per Eco + rarità (+ scuola)
//   strato 3 (sopra): i testi                → costo, nome, scuola, eco, descrizione, ATK, DEF
//
// Perché a strati e non un'unica immagine? Con 100 carte servono solo poche cornici
// + 100 illustrazioni, e i testi (nome, valori) arrivano dal backend: se cambia
// un numero non bisogna ridisegnare l'immagine.
//
// La carta si adatta a QUALSIASI larghezza: tutte le misure sono in "punti carta"
// (la carta è 750 × 1050 punti) e 1 punto = larghezza della carta / 750.
// Basta dare la larghezza con className, es. <CartaGioco carta={...} className="w-40" />
//
// Dati attesi (⚠️ DA CONFERMARE col backend):
// {
//   "codice": "DOM01", "nome": "Cadetto",
//   "atk": 5, "def": 4, "costo": 3,
//   "eco": "DOMINION", "scuola": "WARRIOR", "rarita": "COMMON",   // anche in italiano va bene
//   "effetto": "PIERCING",          // oppure null
//   "descrizione": "Giovane recluta…",   // usata quando non c'è un effetto
//   "illustrazione": "https://…/illustrazioni/DOM01.webp",  // oppure null (mostra un segnaposto)
//   "cornice": "https://…/cornici/dominio_comune.webp"     // il NOME del file decide le zone
// }
// def: se in battaglia la DEF è scesa, passa la DEF attuale (es. { ...carta, def: 1 }).
export default function CartaGioco({ carta: cartaGrezza, className = 'w-full' }) {
  const carta = normalizzaCarta(cartaGrezza)
  const cornice = sceltaCornice(carta) // { nome, src }
  const z = CORNICI[cornice.nome]
  const radice = useRef(null)

  // Calcoli delle posizioni (come nel file del team)
  const yFascia = z.fondoArte + (z.testo[1] - z.fondoArte) * 0.42 // fascia "Scuola · Eco"
  const yValori = z.testo[3] + 8 // ATK e DEF in basso

  // I testi lunghi si rimpiccioliscono finché entrano nel loro spazio
  useLayoutEffect(() => {
    const el = radice.current
    if (!el) return
    const adatta = () => restringiTesti(el)
    adatta()
    document.fonts?.ready.then(adatta) // ricalcola quando i font sono caricati
    // ...e quando la carta cambia dimensione (es. era nascosta e ora si vede)
    const osservatore = new ResizeObserver(adatta)
    osservatore.observe(el)
    return () => osservatore.disconnect()
  }, [carta.nome, carta.descrizione, carta.effetto?.nome, cornice.nome])

  return (
    <article
      ref={radice}
      aria-label={`${carta.nome}: costo ${carta.costo}, ATK ${carta.atk}, DEF ${carta.def}`}
      className={`relative aspect-[750/1050] shrink-0 font-['EB_Garamond',serif] text-[#3a2916] select-none [container-type:inline-size] ${className}`}
      style={{ '--u': 'calc(100cqw / 750)' }}
    >
      {/* 1. Illustrazione (o segnaposto) */}
      {carta.illustrazione ? (
        <img src={carta.illustrazione} alt="" className="absolute object-cover" style={zona(z.arte[0], z.arte[1] - 6, z.arte[2], z.fondoArte + 6)} />
      ) : (
        <div
          className="absolute grid place-items-center bg-[radial-gradient(circle_at_50%_35%,#7d6a58,#2b241d_80%)] font-['Cormorant_Garamond',serif] font-bold tracking-[0.2em] text-[#f4e8d0]/40"
          style={{ ...zona(z.arte[0], z.arte[1] - 6, z.arte[2], z.fondoArte + 6), fontSize: u(26) }}
        >
          ILLUSTRAZIONE
        </div>
      )}

      {/* 2. Cornice */}
      <img src={cornice.src} alt="" className="pointer-events-none absolute inset-0 size-full" />

      {/* 3. Testi. aria-hidden: lo screen reader legge già l'aria-label della carta */}
      <div aria-hidden="true">
        <Valore style={zona(56, 50, 136, 130)}>{carta.costo}</Valore>

        <div
          className="absolute flex items-center justify-center text-center leading-[1.05] font-['Cormorant_Garamond',serif] font-bold"
          style={{ ...zona(z.nome[0] + 10, z.nome[1], z.nome[2] - 10, z.nome[3]), fontSize: u(40) }}
        >
          {carta.nome}
        </div>

        <Fascia style={zona(190, yFascia - 25, 392, yFascia + 25)}>Scuola: {carta.scuola}</Fascia>
        <Fascia style={zona(515, yFascia - 25, 695, yFascia + 25)}>Eco: {carta.eco}</Fascia>

        {/* Pergamena: effetto (se c'è) oppure descrizione in corsivo */}
        <div
          data-adatta={carta.effetto ? 25 : 29}
          className="absolute flex items-center justify-center overflow-hidden"
          style={zona(z.testo[0] + 26, z.testo[1] + 16, z.testo[2] - 26, z.testo[3] - 64)}
        >
          <div className="w-full text-center leading-[1.18]">
            {carta.effetto ? (
              <>
                <b className="mb-[0.25em] block font-bold">Effetto: {carta.effetto.nome}</b>
                <p>{carta.effetto.testo}</p>
              </>
            ) : (
              <i className="font-medium">{carta.descrizione}</i>
            )}
          </div>
        </div>

        <Valore style={zona(135, yValori - 45, 235, yValori + 45)}>{carta.atk}</Valore>
        <Valore style={zona(515, yValori - 45, 615, yValori + 45)}>{carta.def}</Valore>
      </div>
    </article>
  )
}

// n punti carta → misura CSS
const u = (n) => `calc(${n} * var(--u))`

// Rettangolo da (x0, y0) a (x1, y1) in punti carta → stile CSS
const zona = (x0, y0, x1, y1) => ({ left: u(x0), top: u(y0), width: u(x1 - x0), height: u(y1 - y0) })

// Numeri grandi bianchi: costo, ATK, DEF
function Valore({ style, children }) {
  return (
    <div
      className="absolute flex items-center justify-center font-['Playfair_Display',serif] leading-none font-black text-white tabular-nums"
      style={{ ...style, fontSize: u(88), textShadow: `0 ${u(3)} ${u(6)} #000a` }}
    >
      {children}
    </div>
  )
}

// Scritte nella fascia colorata: "Scuola: …" e "Eco: …"
function Fascia({ style, children }) {
  return (
    <div
      data-fascia
      className="absolute flex items-center justify-start overflow-hidden leading-none font-bold whitespace-nowrap text-[#f3e5c2]"
      style={{ ...style, fontSize: u(26) }}
    >
      {children}
    </div>
  )
}

// Rimpicciolisce il testo un punto alla volta finché non esce più dal suo spazio
function restringiTesti(carta) {
  if (!carta.clientWidth) return // carta nascosta: riproveremo quando si vede
  carta.querySelectorAll('[data-adatta]').forEach((el) => {
    let dimensione = Number(el.dataset.adatta)
    do {
      el.style.fontSize = u(dimensione--)
    } while (el.firstChild.offsetHeight > el.clientHeight && dimensione > 14)
  })
  carta.querySelectorAll('[data-fascia]').forEach((el) => {
    let dimensione = 26
    do {
      el.style.fontSize = u(dimensione--)
    } while (el.scrollWidth > el.clientWidth && dimensione > 14)
  })
}
