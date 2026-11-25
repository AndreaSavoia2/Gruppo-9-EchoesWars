import { useCallback, useMemo, useState } from 'react'
import { Link, Navigate, useNavigate, useParams, useSearchParams } from 'react-router'
import AppLayout from '../components/AppLayout'
import Bottone from '../components/Bottone'
import Modale from '../components/Modale'
import Scheletro from '../components/Scheletro'
import MessaggioErrore from '../components/MessaggioErrore'
import FiltriCarteModale from '../components/mazzi/FiltriCarteModale'
import { NESSUN_FILTRO, filtraCarte, quantiFiltri } from '../components/mazzi/filtriCarte'
import { useFetch } from '../hooks/useFetch'
import { getCarteDisponibili, getMazzo, salvaMazzo } from '../api/mazzi'
import { trovaEco } from '../data/echi'
import { REGOLE_MAZZO } from '../data/regole'
import ImmagineCarta from '../components/carta/ImmagineCarta'

/**
 * 5d · 5e · 5n · 5s · 5t / D5
 *   /collezione/mazzi/nuovo/carte?eco=Sapienza&eco=Oblio → nuovo mazzo (passo 2)
 *   /collezione/mazzi/:id                                 → modifica un mazzo che esiste
 *
 * @returns {JSX.Element}
 */
export default function EditorMazzo() {
  const { id } = useParams()
  const [parametri] = useSearchParams()

  // Modifica: prima scarichiamo il mazzo salvato
  const caricaMazzo = useCallback((signal) => (id ? getMazzo(id, signal) : Promise.resolve(null)), [id])
  const { dati: mazzo, caricamento, errore } = useFetch(caricaMazzo)

  if (!id) {
    const echi = parametri.getAll('eco')
    // Senza 2 Echi non si può costruire: torna al passo 1
    if (echi.length !== REGOLE_MAZZO.echi) return <Navigate to="/collezione/mazzi/nuovo" replace />
    return <Editor echi={echi} />
  }

  return caricamento || errore ? (
    <AppLayout paginaAttiva="collezione" sfondoMobile="/images/sfondo-mazzo.png" sfondoDesktop="/images/sfondo-mazzo-desktop.png">
      {caricamento && <Scheletro className="h-40 rounded-2xl" />}
      {errore && <MessaggioErrore testo="Impossibile caricare il mazzo. Riprova più tardi." />}
    </AppLayout>
  ) : (
    <Editor id={id} echi={mazzo.echi} nomeIniziale={mazzo.nome} carteIniziali={mazzo.carte} />
  )
}

/**
 * L'editor del mazzo: carte disponibili, carte nel mazzo, contatori e salvataggio.
 *
 * @param {object} props - le proprietà del componente
 * @param {string|number} props.id
 * @param {*} props.echi
 * @param {string} [props.nomeIniziale='']
 * @param {Array} [props.carteIniziali=[]]
 * @returns {JSX.Element}
 */
function Editor({ id, echi, nomeIniziale = '', carteIniziali = [] }) {
  const navigate = useNavigate()
  // echi.join() = "Sapienza,Oblio": così la richiesta riparte solo se cambiano davvero le Eco
  const chiaveEchi = echi.join()
  const caricaCarte = useCallback((signal) => getCarteDisponibili(chiaveEchi.split(','), signal), [chiaveEchi])
  const { dati: carte, caricamento, errore } = useFetch(caricaCarte)

  // Le carte nel mazzo: { idCarta: copie }. L'ordine di inserimento è l'ordine nel vassoio.
  const [nelMazzo, setNelMazzo] = useState(() =>
    Object.fromEntries(carteIniziali.map(({ cartaId, copie }) => [cartaId, copie])),
  )
  const [filtri, setFiltri] = useState(NESSUN_FILTRO)
  const [filtriAperti, setFiltriAperti] = useState(false)
  const [nomeAperto, setNomeAperto] = useState(false)
  const [nome, setNome] = useState(nomeIniziale)
  const [salvataggio, setSalvataggio] = useState({ inCorso: false, errore: null })

  // Cerca una carta per id
  const perId = useMemo(() => Object.fromEntries((carte ?? []).map((carta) => [carta.id, carta])), [carte])

  // Conti del mazzo
  const righe = Object.entries(nelMazzo)
    .map(([idCarta, copie]) => ({ carta: perId[idCarta], copie }))
    .filter((riga) => riga.carta)
  const totaleCarte = righe.reduce((somma, r) => somma + r.copie, 0)
  const totalePunti = righe.reduce((somma, r) => somma + r.copie * r.carta.punti, 0)
  const mancano = REGOLE_MAZZO.carte - totaleCarte
  const puntiOltre = totalePunti - REGOLE_MAZZO.punti
  const valido = mancano === 0 && puntiOltre <= 0

  // Il vassoio: una casella per ogni copia (es. ×2 → 2 caselle)
  const vassoio = righe.flatMap(({ carta, copie }) => Array.from({ length: copie }, () => carta))

  // Quante copie di una carta puoi ancora mettere
  const limiteCopie = (carta) => Math.min(carta.quantita, REGOLE_MAZZO.copie)

  function aggiungi(carta) {
    const copie = nelMazzo[carta.id] ?? 0
    if (copie >= limiteCopie(carta) || totaleCarte >= REGOLE_MAZZO.carte) return
    setNelMazzo({ ...nelMazzo, [carta.id]: copie + 1 })
  }

  function togli(idCarta) {
    const { [idCarta]: copie, ...altre } = nelMazzo
    setNelMazzo(copie > 1 ? { ...nelMazzo, [idCarta]: copie - 1 } : altre)
  }

  async function salva() {
    setSalvataggio({ inCorso: true, errore: null })
    try {
      await salvaMazzo(
        { nome: nome.trim(), echi, carte: righe.map(({ carta, copie }) => ({ cartaId: carta.id, copie })) },
        id,
      )
      navigate('/collezione/mazzi')
    } catch {
      setSalvataggio({ inCorso: false, errore: 'Salvataggio non riuscito. Riprova.' })
    }
  }

  const titolo = id ? nomeIniziale : 'Nuovo mazzo'
  const carteVisibili = carte ? filtraCarte(carte, filtri) : []
  const numeroFiltri = quantiFiltri(filtri)
  const chiudiFiltri = useCallback(() => setFiltriAperti(false), [])
  const chiudiNome = useCallback(() => setNomeAperto(false), [])

  return (
    <AppLayout
      paginaAttiva="collezione"
      sfondoMobile="/images/sfondo-mazzo.png"
      sfondoDesktop="/images/sfondo-mazzo-desktop.png"
      nascondiMenuMobile
    >
      <div className="-mx-5 -mt-8 lg:m-0 lg:flex lg:flex-col lg:gap-6">
        {/* 📱 Mobile: pannello del mazzo fisso in alto */}
        <div className="sticky top-18 z-10 flex flex-col gap-2.5 border-b border-oro-bordo bg-sfondo px-5 pt-4 pb-3 shadow-[0_6px_14px_rgba(33,38,43,0.12)] lg:hidden">
          <div className="flex items-center gap-2.5">
            <Link to="/collezione/mazzi" className="flex min-h-11 items-center font-medium text-testo-secondario">
              ‹ Mazzi
            </Link>
            <h1 className="truncate pl-3 font-titolo text-[22px]">{titolo}</h1>
          </div>
          <Contatori echi={echi} totaleCarte={totaleCarte} totalePunti={totalePunti} puntiOltre={puntiOltre} />
          <Vassoio carte={vassoio} onTogli={togli} />
          {puntiOltre > 0 && <AvvisoPunti />}
        </div>

        {/* 💻 Desktop: intestazione */}
        <div className="hidden flex-col gap-1.5 lg:flex">
          <h1 className="font-titolo text-h2">{titolo}</h1>
          <p>
            {id ? 'Modifica le carte del mazzo.' : 'Passo 2 di 2'} · Aggiungi {REGOLE_MAZZO.carte} carte di {echi.join(' e ')}.
          </p>
          <Link to="/collezione/mazzi" className="self-start font-medium underline">
            ‹ Mazzi
          </Link>
        </div>

        <div className="flex flex-col gap-3.5 px-5 pt-4 pb-28 lg:flex-row lg:items-start lg:gap-6 lg:p-0">
          {/* La tua collezione (solo le 2 Eco) */}
          <section className="flex flex-col gap-3.5 bg-sfondo/86 lg:flex-1 lg:gap-5 lg:rounded-2xl lg:border lg:border-oro lg:bg-sfondo lg:p-6">
            <div className="flex items-center justify-between gap-3">
              <div className="flex flex-col gap-0.5 leading-[1.4] lg:flex-row lg:items-baseline lg:gap-3">
                <h2 className="font-semibold lg:font-titolo lg:text-h2 lg:font-normal">La tua collezione</h2>
                <p className="text-didascalia font-medium">Solo {echi.join(' e ')}</p>
              </div>
              <Bottone variante="secondario" onClick={() => setFiltriAperti(true)} className="w-auto min-w-37 shrink-0 lg:min-w-50">
                ☰ Filtri{numeroFiltri > 0 && ` (${numeroFiltri})`}
              </Bottone>
            </div>

            {caricamento && (
              <div className="grid grid-cols-3 gap-3">
                {[1, 2, 3].map((n) => (
                  <Scheletro key={n} className="aspect-[107/150] rounded-lg" />
                ))}
              </div>
            )}
            {errore && <MessaggioErrore />}
            {carte && carteVisibili.length === 0 && <p>Nessuna carta corrisponde ai filtri.</p>}

            {carteVisibili.length > 0 && (
              <ul className="grid grid-cols-3 gap-x-3 gap-y-3.5 lg:grid-cols-[repeat(auto-fill,minmax(88px,1fr))] lg:gap-y-4">
                {carteVisibili.map((carta) => {
                  const copie = nelMazzo[carta.id] ?? 0
                  return (
                    <li key={carta.id}>
                      <CartaPerMazzo
                        carta={carta}
                        copie={copie}
                        limite={limiteCopie(carta)}
                        mazzoPieno={totaleCarte >= REGOLE_MAZZO.carte}
                        onAggiungi={() => aggiungi(carta)}
                      />
                    </li>
                  )
                })}
              </ul>
            )}
          </section>

          {/* 💻 Desktop: "Il tuo mazzo" a destra */}
          <aside className="sticky top-24 hidden w-75 shrink-0 flex-col gap-4 rounded-2xl border border-oro bg-sfondo p-4 lg:flex">
            <h2 className="font-titolo text-h2">Il tuo mazzo</h2>
            <Barra etichetta="Carte" valore={totaleCarte} massimo={REGOLE_MAZZO.carte} testo={`${totaleCarte}/${REGOLE_MAZZO.carte}`} />
            <Barra etichetta="Costo totale" valore={totalePunti} massimo={REGOLE_MAZZO.punti} testo={`${totalePunti}/${REGOLE_MAZZO.punti} punti`} oltre={puntiOltre > 0} />
            <p className="text-didascalia leading-[1.4]">
              Regole: solo i {REGOLE_MAZZO.echi} Echi scelti · massimo {REGOLE_MAZZO.copie} copie per carta · costo massimo{' '}
              {REGOLE_MAZZO.punti} punti
            </p>
            {/* Con tante carte la lista scorre dentro, così "Salva" resta visibile */}
            <ul className="flex max-h-[38vh] flex-col gap-1.5 overflow-y-auto">
              {righe.map(({ carta, copie }) => (
                <li key={carta.id} className="flex items-center gap-2 rounded-xl bg-sfondo-secondario py-1.5 pr-1.5 pl-2">
                  <span className="flex-1 truncate font-medium">{carta.nome}</span>
                  <span className="text-didascalia">
                    {carta.punti} {carta.punti === 1 ? 'punto' : 'punti'}
                  </span>
                  <span className="font-semibold">×{copie}</span>
                  <button
                    type="button"
                    onClick={() => togli(carta.id)}
                    aria-label={`Togli una copia di ${carta.nome}`}
                    className="flex size-8 items-center justify-center rounded-full border border-testo hover:bg-sfondo"
                  >
                    −
                  </button>
                </li>
              ))}
            </ul>
            <StatoMazzo mancano={mancano} puntiOltre={puntiOltre} />
            <Bottone onClick={() => setNomeAperto(true)} disabled={!valido} className="min-h-12 w-full">
              Salva mazzo
            </Bottone>
          </aside>
        </div>
      </div>

      {/* 📱 Mobile: barra in basso con lo stato e "Salva mazzo" */}
      <div className="fixed inset-x-0 bottom-0 z-10 flex h-24 items-center gap-3 border-t border-oro-bordo bg-sfondo px-5 pb-5 lg:hidden">
        <p className={`text-didascalia leading-[1.4] font-medium ${puntiOltre > 0 ? 'text-errore' : ''}`} aria-live="polite">
          {puntiOltre > 0
            ? `⚠ ${puntiOltre} punti oltre il limite di ${REGOLE_MAZZO.punti}`
            : mancano > 0
              ? `Mancano ${mancano} ${mancano === 1 ? 'carta' : 'carte'}`
              : '✓ Mazzo completo'}
        </p>
        <Bottone onClick={() => setNomeAperto(true)} disabled={!valido} className="flex-1 disabled:opacity-55">
          Salva mazzo
        </Bottone>
      </div>

      {filtriAperti && carte && (
        <FiltriCarteModale
          carte={carte}
          filtri={filtri}
          onChiudi={chiudiFiltri}
          onApplica={(nuovi) => {
            setFiltri(nuovi)
            setFiltriAperti(false)
          }}
        />
      )}

      {/* 5s · Dai un nome al mazzo */}
      {nomeAperto && (
        <Modale titolo="Dai un nome al mazzo" onChiudi={chiudiNome}>
          <p className="text-testo-secondario">
            Il mazzo è completo: {totaleCarte} carte, {totalePunti}/{REGOLE_MAZZO.punti} punti, {echi.join(' + ')}.
          </p>
          <label className="flex flex-col gap-2 leading-[1.4] font-semibold">
            Nome del mazzo
            <input
              type="text"
              value={nome}
              onChange={(evento) => setNome(evento.target.value)}
              maxLength={REGOLE_MAZZO.lunghezzaNome}
              className="h-12 rounded-xl border-[1.5px] border-testo bg-sfondo px-4 font-normal"
            />
          </label>
          <p className="text-didascalia leading-[1.4] font-medium">
            Massimo {REGOLE_MAZZO.lunghezzaNome} caratteri · puoi cambiarlo dopo
          </p>
          <Bottone onClick={salva} disabled={!nome.trim() || salvataggio.inCorso}>
            {salvataggio.inCorso ? 'Salvataggio…' : 'Salva mazzo'}
          </Bottone>
          <Bottone variante="secondario" onClick={chiudiNome}>
            Torna al mazzo
          </Bottone>
          {salvataggio.errore && <MessaggioErrore testo={salvataggio.errore} />}
        </Modale>
      )}
    </AppLayout>
  )
}

/**
 * Pillole in alto: carte, punti e le 2 Eco
 *
 * @param {object} props - le proprietà del componente
 * @param {*} props.echi
 * @param {*} props.totaleCarte
 * @param {*} props.totalePunti
 * @param {*} props.puntiOltre
 * @returns {JSX.Element}
 */
function Contatori({ echi, totaleCarte, totalePunti, puntiOltre }) {
  const pillola = 'rounded-full border-[1.2px] px-2.5 py-1 text-didascalia leading-[1.4] font-medium'
  return (
    <div className="flex flex-wrap gap-1.5">
      <span className={`${pillola} border-testo bg-sfondo-secondario`}>
        {totaleCarte}/{REGOLE_MAZZO.carte} carte
      </span>
      <span className={`${pillola} ${puntiOltre > 0 ? 'border-errore bg-errore-sfondo text-errore' : 'border-testo bg-sfondo-secondario'}`}>
        {totalePunti}/{REGOLE_MAZZO.punti} punti
      </span>
      {echi.map((nome) => {
        const eco = trovaEco(nome)
        return (
          <span key={nome} className={`${pillola} bg-sfondo ${eco?.bordo} ${eco?.testo}`}>
            {nome}
          </span>
        )
      })}
    </div>
  )
}

/**
 * Il "vassoio": 30 caselle su 2 righe. Tocca una carta per toglierla.
 *
 * @param {object} props - le proprietà del componente
 * @param {*} props.carte
 * @param {Function} props.onTogli
 * @returns {JSX.Element}
 */
function Vassoio({ carte, onTogli }) {
  const caselle = Array.from({ length: REGOLE_MAZZO.carte }, (_, i) => carte[i])
  return (
    <div className="h-36.5 overflow-x-auto rounded-xl bg-[rgba(33,38,43,0.06)] p-2">
      <ul className="grid w-max grid-cols-[repeat(15,44px)] grid-rows-2 gap-1.5">
        {caselle.map((carta, i) => (
          <li key={i}>
            {carta ? (
              <button type="button" onClick={() => onTogli(carta.id)} aria-label={`Togli ${carta.nome}`} className="block">
                <ImmagineCarta carta={carta} className="h-15.5 w-11" />
              </button>
            ) : (
              <span className="block h-15.5 w-11 rounded-[5px] border-[1.2px] border-dashed border-oro-bordo bg-sfondo/60" />
            )}
          </li>
        ))}
      </ul>
    </div>
  )
}

/**
 * Una carta della collezione: toccala per aggiungerla al mazzo
 *
 * @param {object} props - le proprietà del componente
 * @param {*} props.carta
 * @param {*} props.copie
 * @param {*} props.limite
 * @param {*} props.mazzoPieno
 * @param {Function} props.onAggiungi
 * @returns {JSX.Element}
 */
function CartaPerMazzo({ carta, copie, limite, mazzoPieno, onAggiungi }) {
  const esaurita = copie >= limite
  const eco = trovaEco(carta.eco)
  let statoDesktop = 'Non nel mazzo'
  if (copie > 0) statoDesktop = copie >= carta.quantita ? `Nel mazzo ${copie} · tutte le tue copie` : `Nel mazzo ${copie}/${limite}`

  return (
    <button
      type="button"
      onClick={onAggiungi}
      disabled={esaurita || mazzoPieno}
      aria-label={`Aggiungi ${carta.nome}`}
      className="flex w-full flex-col items-start gap-1 text-left text-didascalia leading-[1.4] font-medium disabled:cursor-not-allowed"
    >
      <span className={`relative block aspect-[107/150] w-full ${esaurita ? 'lg:opacity-50' : ''}`}>
        <ImmagineCarta carta={carta} className="size-full" />
        <span className="absolute -top-1.5 right-1 rounded-full border-[1.5px] border-sfondo bg-testo px-1.75 py-0.5 text-sfondo">
          ×{carta.quantita}
        </span>
        {copie > 0 && (
          <span className="absolute bottom-1 left-1 rounded-full border border-testo bg-oro px-1.75 py-0.5 lg:hidden">
            Nel mazzo {copie}
          </span>
        )}
      </span>
      <span className="lg:font-semibold">{carta.nome}</span>
      <span className={`${eco?.testo} lg:text-testo`}>
        {carta.punti} {carta.punti === 1 ? 'punto' : 'punti'} · {carta.eco}
      </span>
      <span className="hidden lg:block">{statoDesktop}</span>
    </button>
  )
}

/**
 * Barra di avanzamento (desktop): Carte e Costo totale
 *
 * @param {object} props - le proprietà del componente
 * @param {string} props.etichetta
 * @param {*} props.valore
 * @param {*} props.massimo
 * @param {string} props.testo
 * @param {boolean} [props.oltre=false]
 * @returns {JSX.Element}
 */
function Barra({ etichetta, valore, massimo, testo, oltre = false }) {
  return (
    <div className="flex flex-col gap-1.5 leading-[1.4]">
      <div className="flex justify-between font-semibold">
        <span>{etichetta}</span>
        <span className={oltre ? 'text-errore' : ''}>{testo}</span>
      </div>
      <div className="h-2 overflow-hidden rounded border border-testo-secondario bg-sfondo-secondario">
        <div className={`h-full ${oltre ? 'bg-errore' : 'bg-oro-bordo'}`} style={{ width: `${Math.min(100, (valore / massimo) * 100)}%` }} />
      </div>
    </div>
  )
}

/**
 * Avviso quando il mazzo supera i punti massimi.
 *
 * @returns {JSX.Element}
 */
function AvvisoPunti() {
  return (
    <p role="alert" className="flex gap-2.5 rounded-xl border border-errore bg-errore-sfondo px-3.5 py-3 text-didascalia leading-[1.4] font-medium">
      <span aria-hidden="true" className="text-errore">⚠</span>
      Hai superato il costo massimo. Togli una carta o scegline una che costa meno.
    </p>
  )
}

/**
 * Messaggio sotto la lista (desktop)
 *
 * @param {object} props - le proprietà del componente
 * @param {*} props.mancano
 * @param {*} props.puntiOltre
 * @returns {JSX.Element}
 */
function StatoMazzo({ mancano, puntiOltre }) {
  if (puntiOltre > 0) return <AvvisoPunti />
  if (mancano > 0) {
    return (
      <p className="rounded-xl border border-info bg-info-sfondo px-3.5 py-3 text-didascalia leading-[1.4] font-medium">
        Mancano {mancano} {mancano === 1 ? 'carta' : 'carte'} per completare il mazzo.
      </p>
    )
  }
  return (
    <p className="rounded-xl border border-successo bg-successo-sfondo px-3.5 py-3 text-didascalia leading-[1.4] font-medium">
      ✓ Mazzo completo: puoi salvarlo.
    </p>
  )
}
