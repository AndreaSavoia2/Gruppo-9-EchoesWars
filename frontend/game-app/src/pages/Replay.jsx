import { useCallback, useEffect, useState } from 'react'
import { Link, useNavigate, useParams } from 'react-router'
import Scheletro from '../components/Scheletro'
import MessaggioErrore from '../components/MessaggioErrore'
import LatoGiocatore from '../components/replay/LatoGiocatore'
import ImmagineCarta from '../components/carta/ImmagineCarta'
import { useFetch } from '../hooks/useFetch'
import { useUtente } from '../context/UtenteContext'
import { getReplay } from '../api/partite'

// Quanto dura ogni momento del round (in millisecondi)
const DURATA = { carte: 1500, scontro: 1500, esito: 2000 }

// G7 / G7b / G7c / DG6 · battaglia automatica a schermo intero. Si usa in 2 punti:
//   /partita/:id/battaglia → subito dopo il matchmaking (modo="battaglia")
//       fine → schermata "Hai vinto / Hai perso"   ·   Esci → Home
//   /partita/:id/replay    → "Rivedi partita" dai dettagli (modo="replay")
//       fine → torna ai dettagli                     ·   Esci → torna ai dettagli
export default function Replay({ modo = 'replay' }) {
  const { id } = useParams()
  const navigate = useNavigate()
  const caricaReplay = useCallback((signal) => getReplay(id, signal), [id])
  const { dati: replay, caricamento, errore } = useFetch(caricaReplay)
  const { dati: utente } = useUtente()

  // A che punto siamo: quale round (indice) e quale momento
  const [indice, setIndice] = useState(0)
  const [momento, setMomento] = useState('carte') // 'carte' → 'scontro' → 'esito'

  const dettagli = `/partita/${id}/dettagli`
  const fine = modo === 'battaglia' ? `/partita/${id}` : dettagli
  const uscita = modo === 'battaglia' ? '/' : dettagli
  const round = replay?.round[indice]

  // Fa avanzare il replay da solo, un momento alla volta
  useEffect(() => {
    if (!round) return
    const timer = setTimeout(() => {
      if (momento === 'carte') setMomento('scontro')
      else if (momento === 'scontro') setMomento('esito')
      else if (indice < replay.round.length - 1) {
        setIndice(indice + 1)
        setMomento('carte')
      } else {
        // finito! replace = il tasto "indietro" non fa ripartire la battaglia
        navigate(fine, { replace: true })
      }
    }, DURATA[momento])
    return () => clearTimeout(timer)
  }, [round, momento, indice, replay, navigate, fine])

  // Tasto Esc = come il bottone "Esci"
  useEffect(() => {
    const premi = (evento) => evento.key === 'Escape' && navigate(uscita, { replace: true })
    window.addEventListener('keydown', premi)
    return () => window.removeEventListener('keydown', premi)
  }, [navigate, uscita])

  // Nel momento "esito" mostriamo la situazione di FINE round
  const stato = momento === 'esito' ? round?.dopo : round
  const inScontro = momento === 'scontro'

  return (
    <main className="relative isolate flex min-h-screen flex-col items-center justify-between overflow-hidden bg-testo py-2 lg:justify-center lg:gap-4 lg:py-8">
      {/* Sfondo arena scurito */}
      <picture aria-hidden="true">
        <source media="(min-width: 1024px)" srcSet="/images/modalita-casuale.png" />
        <img src="/images/sfondo-gioca.png" alt="" className="absolute inset-0 -z-10 size-full object-cover opacity-40" />
      </picture>

      {/* Bottone per uscire: in battaglia porta alla Home, nel replay ai dettagli.
          Il risultato resta comunque nella Cronologia. */}
      {/* replace = col tasto "indietro" del browser non si rientra nella battaglia */}
      <Link
        to={uscita}
        replace
        className="absolute top-1.5 left-3 z-10 flex min-h-11 w-20 items-center justify-center rounded-full border-[1.5px] border-testo bg-sfondo font-medium hover:bg-sfondo-secondario lg:top-6 lg:left-6"
      >
        Esci
      </Link>

      {caricamento && <Scheletro className="m-auto h-40 w-72 rounded-2xl" />}
      {errore && (
        <div className="m-auto rounded-2xl bg-sfondo p-4">
          <MessaggioErrore testo="Impossibile caricare il replay." />
        </div>
      )}

      {stato && (
        <>
          <div className="mt-12 lg:mt-0">
            <LatoGiocatore nome={replay.avversario.nome} stato={stato.avversario} inAlto nascondiCarta={inScontro} />
          </div>

          {/* Centro: numero del round, scontro o messaggio di esito */}
          <div className="relative flex flex-1 items-center justify-center lg:flex-none" aria-live="polite">
            {inScontro ? (
              <Scontro round={round} />
            ) : momento === 'esito' && round.esito ? (
              <div className="rounded-2xl bg-sfondo px-5 py-3 text-center leading-[1.4]">
                <p className="font-semibold">{round.esito.titolo}</p>
                <p className="text-didascalia font-medium text-errore">{round.esito.dettaglio}</p>
              </div>
            ) : (
              <p className="rounded-full border-[1.5px] border-oro-bordo bg-oro px-3.5 py-1.5 leading-[1.4] font-semibold lg:border-2 lg:px-5 lg:py-2 lg:text-[22px]">
                Round {round.numero} / {replay.roundMax}
              </p>
            )}
          </div>

          <LatoGiocatore nome={`Tu · ${utente?.nome ?? ''}`} stato={stato.tu} nascondiCarta={inScontro} />

          <p className="mt-2 hidden rounded-full bg-sfondo px-4.5 py-2.5 text-didascalia leading-[1.4] font-medium lg:block">
            La battaglia è automatica: guarda come si scontrano le carte.
          </p>
        </>
      )}
    </main>
  )
}

// Le due carte si scontrano al centro, con i danni subiti
function Scontro({ round }) {
  const { avversario, tu } = round
  return (
    <div className="relative h-72 w-56">
      <div className="absolute top-0 right-4 rotate-6">
        <ImmagineCarta carta={avversario.carta} className="h-44 w-31" />
      </div>
      <div className="absolute bottom-0 left-4 -rotate-6">
        <ImmagineCarta carta={tu.carta} className="h-44 w-31" />
      </div>
      {round.dannoAvversario > 0 && <Danno valore={round.dannoAvversario} className="-top-2 -right-6" />}
      {round.dannoTu > 0 && <Danno valore={round.dannoTu} className="bottom-6 -left-8" />}
    </div>
  )
}

function Danno({ valore, className }) {
  return (
    <span
      className={`absolute font-titolo text-[40px] text-errore [text-shadow:0_0_2px_#fff9f0,0_0_2px_#fff9f0] ${className}`}
    >
      −{valore}
    </span>
  )
}
