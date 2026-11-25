import { useEffect, useState } from 'react'
import ImmagineCarta from '../carta/ImmagineCarta'

// Animazione di apertura (dal Figma "Esempio · Animazione apertura busta"): 6 fasi
//   1 busta    → tocca la busta
//   2 carica   → la busta si ingrandisce e si inclina      (automatica)
//   3 scossa   → la busta trema                            (automatica)
//   4 apertura → le carte escono dalla busta               (automatica)
//   5 ventaglio→ le carte si aprono a ventaglio → tocca
//   6 risultato→ "Busta aperta!" con le carte girate
// "Spostamento" di ogni elemento rispetto al centro dello schermo: [x, y, rotazione, scala]
const BUSTA = {
  busta: [0, -47, 0, 1],
  carica: [0, -47, -4, 1.2],
  scossa: [0, -47, 4, 1.2],
  apertura: [0, 61, 0, 1],
  ventaglio: [0, 300, 0, 1],
}
const BAGLIORE = { busta: 240, carica: 330, scossa: 360, apertura: 440, ventaglio: 400 }
// Le 5 carte: nascoste nella busta → in pila sopra la busta → a ventaglio
const CARTE = {
  apertura: [
    [-4, -92, -3],
    [-2, -90, -1],
    [0, -88, 0],
    [2, -86, 1],
    [4, -84, 3],
  ],
  ventaglio: [
    [-104, -34, -20],
    [-54, -50, -10],
    [0, -54, 0],
    [54, -50, 10],
    [104, -34, 20],
  ],
}
const DURATA = { carica: 500, scossa: 500, apertura: 800 }
const AUTOMATICHE = { carica: 'scossa', scossa: 'apertura', apertura: 'ventaglio' }

function trasforma([x, y, rotazione = 0, scala = 1]) {
  return { transform: `translate(-50%, -50%) translate(${x}px, ${y}px) rotate(${rotazione}deg) scale(${scala})` }
}

// carte: quelle arrivate dal backend · riepilogo: testo · onFine: "Vedi le carte"
export default function AperturaBusta({ carte, riepilogo, onFine }) {
  const [fase, setFase] = useState('busta')

  // Le fasi automatiche passano da sole alla successiva
  useEffect(() => {
    const prossima = AUTOMATICHE[fase]
    if (!prossima) return
    const timer = setTimeout(() => setFase(prossima), DURATA[fase])
    return () => clearTimeout(timer)
  }, [fase])

  const primeCinque = carte.slice(0, 5)
  const carteVisibili = fase === 'apertura' || fase === 'ventaglio'

  return (
    <div className="fixed inset-0 z-50 overflow-y-auto bg-testo text-sfondo" role="dialog" aria-modal="true" aria-label="Apertura busta">
      {fase !== 'risultato' ? (
        <div className="relative mx-auto h-full min-h-[600px] w-full max-w-[402px] overflow-hidden">
          {/* Titolo: sparisce piano piano */}
          <p
            className={`absolute top-[12.5%] w-full text-center font-titolo text-[26px] transition-opacity duration-500 ${
              fase === 'busta' ? 'opacity-100' : fase === 'carica' ? 'opacity-40' : 'opacity-0'
            }`}
          >
            Echoes’ War First Edition
          </p>

          {/* Bagliore dietro la busta: cresce a ogni fase */}
          <img
            src="/icons/bagliore.svg"
            alt=""
            className="pointer-events-none absolute top-1/2 left-1/2 max-w-none transition-all duration-500"
            style={{
              width: BAGLIORE[fase] * 1.58,
              height: BAGLIORE[fase] * 1.58,
              opacity: fase === 'apertura' ? 1 : 0.8,
              ...trasforma([0, fase === 'ventaglio' ? -40 : -47]),
            }}
          />

          {/* La busta (è un bottone: si tocca per aprirla) */}
          <button
            type="button"
            onClick={() => fase === 'busta' && setFase('carica')}
            disabled={fase !== 'busta'}
            aria-label="Apri la busta"
            className={`absolute top-1/2 left-1/2 h-70 w-50 rounded-[14px] border-3 border-oro-bordo bg-oro transition-all duration-500 ${
              fase === 'ventaglio' ? 'opacity-0' : 'opacity-100'
            } ${fase === 'scossa' ? 'motion-safe:animate-[scossa_0.15s_ease-in-out_3]' : ''}`}
            style={trasforma(BUSTA[fase])}
          >
            <span className="absolute -top-0.75 -left-0.75 h-14 w-50 rounded-t-[14px] bg-oro-bordo" />
            <img src="/icons/stella.png" alt="" className="absolute top-25.5 left-13 size-22.5 object-contain" />
          </button>

          {/* Le 5 carte coperte (retro con la stella) */}
          {primeCinque.map((carta, i) => {
            const posizione = carteVisibili ? CARTE[fase][i] : [0, -69]
            return (
              <div
                key={carta.id ?? i}
                className={`pointer-events-none absolute top-1/2 left-1/2 flex h-31.5 w-22.5 items-center justify-center rounded-lg border-2 border-oro-bordo bg-sfondo-secondario transition-all duration-700 ${
                  carteVisibili ? 'opacity-100' : 'scale-60 opacity-0'
                }`}
                style={{ ...trasforma(posizione), transitionDelay: `${i * 40}ms` }}
              >
                <img src="/icons/stella.png" alt="" className="size-11 object-contain" />
              </div>
            )
          })}

          {/* Suggerimento in basso */}
          {(fase === 'busta' || fase === 'ventaglio') && (
            <p className="absolute top-[74%] w-full text-center">
              {fase === 'busta' ? 'Tocca la busta per aprirla' : 'Tocca per scoprire le carte'}
            </p>
          )}

          {/* Nel ventaglio: tutto lo schermo è toccabile per scoprire le carte */}
          {fase === 'ventaglio' && (
            <button type="button" onClick={() => setFase('risultato')} aria-label="Scopri le carte" className="absolute inset-0" />
          )}
        </div>
      ) : (
        <Risultato carte={carte} riepilogo={riepilogo} onFine={onFine} />
      )}
    </div>
  )
}

// Fase 6 · "Busta aperta!": le carte girate. Rare e leggendarie con il bordo che brilla.
function Risultato({ carte, riepilogo, onFine }) {
  const bordo = {
    Rara: 'border-3 border-info shadow-[0_0_9px_rgba(74,64,140,0.7)]',
    Leggendaria: 'border-3 border-oro-bordo shadow-[0_0_9px_rgba(184,138,67,0.8)]',
  }
  return (
    <div className="relative mx-auto flex min-h-full w-full max-w-[402px] flex-col items-center gap-6 px-5 py-[12.5vh] text-center lg:max-w-2xl">
      <img src="/icons/bagliore.svg" alt="" className="pointer-events-none absolute top-[40%] left-1/2 size-[440px] max-w-none -translate-1/2" />
      <div className="relative flex flex-col gap-3">
        <h2 className="font-titolo text-[26px]">Busta aperta!</h2>
        <p>{riepilogo}</p>
      </div>
      <ul className="relative flex flex-wrap justify-center gap-x-7.5 gap-y-21">
        {carte.map((carta, i) => (
          <li key={`${carta.id}-${i}`} className="motion-safe:animate-[comparsa_0.4s_ease-out_both]" style={{ animationDelay: `${i * 80}ms` }}>
            <ImmagineCarta carta={carta} className={`h-31.5 w-22.5 ${bordo[carta.rarita] ?? ''}`} />
          </li>
        ))}
      </ul>
      <button
        type="button"
        onClick={onFine}
        className="relative mt-auto rounded-full border border-testo bg-oro px-8 py-3 font-medium text-testo shadow-[0_6px_8px_rgba(0,0,0,0.15)]"
      >
        Vedi le carte
      </button>
    </div>
  )
}
