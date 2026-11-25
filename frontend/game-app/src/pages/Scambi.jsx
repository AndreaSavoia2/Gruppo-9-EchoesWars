import { useCallback, useState } from 'react'
import { useNavigate, useSearchParams } from 'react-router'
import AppLayout from '../components/AppLayout'
import Intestazione from '../components/Intestazione'
import Schede from '../components/Schede'
import Avviso from '../components/Avviso'
import Modale from '../components/Modale'
import Bottone, { BottoneLink } from '../components/Bottone'
import Scheletro from '../components/Scheletro'
import MessaggioErrore from '../components/MessaggioErrore'
import CoppiaCarte from '../components/scambi/CoppiaCarte'
import { useFetch } from '../hooks/useFetch'
import { annullaProposta, getScambi, rifiutaScambio } from '../api/scambi'
import { REGOLE_SCAMBI } from '../data/regole'
import { tempoMancante } from '../utils/testo'

const etichetteStato = { accettata: '✓ Accettato', rifiutata: 'Rifiutato', scaduta: 'Scaduto', annullata: 'Annullato' }

// 10 · 10a · 10g · 10j / D9 · /scambi?scheda=ricevute|inviate|storico
export default function Scambi() {
  const [parametri] = useSearchParams()
  const scheda = parametri.get('scheda') ?? 'ricevute'
  const { dati, caricamento, errore, ricarica } = useFetch(getScambi)
  const [daAnnullare, setDaAnnullare] = useState(null)
  const chiudiAnnulla = useCallback(() => setDaAnnullare(null), [])

  async function rifiuta(id) {
    await rifiutaScambio(id)
    ricarica()
  }

  async function annulla() {
    await annullaProposta(daAnnullare.id)
    setDaAnnullare(null)
    ricarica()
  }

  // 10a · Mercato degli Echi ancora bloccato
  if (dati?.bloccato) {
    return (
      <AppLayout paginaAttiva="scambi">
        <div className="flex flex-col gap-5 lg:max-w-md">
          <Intestazione titolo="Scambi" sottotitolo="Scambia carte con i tuoi amici." />
          <MercatoBloccato {...dati.bloccato} />
        </div>
      </AppLayout>
    )
  }

  const inPausa = Boolean(dati?.pausa)
  const schede = [
    { id: 'ricevute', etichetta: `Ricevute${dati ? ` (${dati.ricevute.length})` : ''}`, percorso: '/scambi' },
    { id: 'inviate', etichetta: `Inviate${dati ? ` (${dati.inviate.length})` : ''}`, percorso: '/scambi?scheda=inviate' },
    { id: 'storico', etichetta: 'Storico', percorso: '/scambi?scheda=storico' },
  ]
  const elenco = dati?.[scheda] ?? []

  const bottoneNuova = (
    <BottoneLink
      to="/scambi/nuova"
      variante="secondario"
      aria-disabled={inPausa}
      onClick={(evento) => inPausa && evento.preventDefault()}
      className={`w-full lg:w-auto ${inPausa ? 'pointer-events-none opacity-40' : ''}`}
    >
      Nuova proposta
    </BottoneLink>
  )

  return (
    <AppLayout paginaAttiva="scambi">
      <div className="flex flex-col gap-3 lg:gap-6">
        <Intestazione titolo="Scambi" sottotitolo="Una carta contro una della stessa rarità." />

        <div className="flex items-center justify-between gap-6 pt-3 lg:pt-0">
          <Schede schede={schede} attiva={scheda} etichetta="Proposte di scambio" />
          <div className="hidden lg:block">{bottoneNuova}</div>
        </div>

        <div className="flex flex-col gap-6 lg:flex-row lg:items-start">
          <div className="flex flex-1 flex-col gap-3 lg:gap-4">
            {inPausa ? (
              <Avviso tipo="errore" icona="⏸️" barra>
                Scambi in pausa per {REGOLE_SCAMBI.orePausa} ore dopo lo scambio con {dati.pausa.conAmico}. Potrai accettare o
                proporre scambi tra {tempoMancante(dati.pausa.fineIl, true)}. Le proposte ricevute restano qui.
              </Avviso>
            ) : (
              scheda === 'ricevute' && (
                <Avviso tipo="info" barra>
                  Una proposta scade dopo {REGOLE_SCAMBI.oreProposta} ore se non rispondi. Dopo uno scambio accettato gli
                  scambi si fermano per {REGOLE_SCAMBI.orePausa} ore.
                </Avviso>
              )
            )}

            {caricamento && <Scheletro className="h-64 rounded-2xl" />}
            {errore && <MessaggioErrore />}
            {dati && elenco.length === 0 && <p>{testoVuoto[scheda]}</p>}

            {elenco.length > 0 && (
              <ul className="grid gap-3 lg:grid-cols-2 lg:gap-4">
                {elenco.map((proposta) => (
                  <li key={proposta.id}>
                    {scheda === 'ricevute' && (
                      <PropostaRicevuta proposta={proposta} inPausa={inPausa} onRifiuta={() => rifiuta(proposta.id)} />
                    )}
                    {scheda === 'inviate' && <PropostaInviata proposta={proposta} onAnnulla={() => setDaAnnullare(proposta)} />}
                    {scheda === 'storico' && <PropostaStorico proposta={proposta} />}
                  </li>
                ))}
              </ul>
            )}

            <div className="lg:hidden">{bottoneNuova}</div>
          </div>

          {/* 💻 Desktop: le regole a destra */}
          <aside className="hidden w-100 shrink-0 flex-col gap-3 rounded-2xl border border-oro bg-sfondo p-4 lg:flex">
            <h2 className="font-titolo text-h2">Come funzionano gli scambi</h2>
            <ul className="flex flex-col gap-2 text-didascalia">
              <li>• Scambi carte solo con i tuoi amici.</li>
              <li>• 1 carta in cambio di 1 carta della stessa rarità.</li>
              <li>• Una proposta vale {REGOLE_SCAMBI.oreProposta} ore, poi scade se non rispondi.</li>
              <li>• Dopo uno scambio accettato, gli scambi si fermano per {REGOLE_SCAMBI.orePausa} ore.</li>
              <li>• Il Mercato degli Echi si sblocca dopo {REGOLE_SCAMBI.partitePerSbloccare} partite valide.</li>
            </ul>
          </aside>
        </div>
      </div>

      {daAnnullare && (
        <Modale titolo="Annullare la proposta?" onChiudi={chiudiAnnulla}>
          <p className="text-testo-secondario">
            La proposta a {daAnnullare.amico.nome} verrà ritirata e {daAnnullare.offerta.nome} tornerà disponibile.
          </p>
          <Bottone variante="distruttivo" onClick={annulla}>
            Annulla proposta
          </Bottone>
          <Bottone variante="secondario" onClick={chiudiAnnulla}>
            Tieni la proposta
          </Bottone>
        </Modale>
      )}
    </AppLayout>
  )
}

const testoVuoto = {
  ricevute: 'Nessuna proposta ricevuta per ora.',
  inviate: 'Non hai proposte in attesa.',
  storico: 'Non hai ancora fatto scambi.',
}

const riquadro = 'flex flex-col gap-2 rounded-2xl border border-oro-bordo bg-sfondo px-4 py-3.5'

// Proposta che hai ricevuto: "Ti offre" ⇄ "Vuole"
function PropostaRicevuta({ proposta, inPausa, onRifiuta }) {
  const navigate = useNavigate()
  return (
    <article className={riquadro}>
      <div className="flex justify-between leading-[1.4]">
        <h3 className="font-semibold">{proposta.amico.nome}</h3>
        <p className="text-didascalia font-medium text-testo-secondario">Scade tra {tempoMancante(proposta.scadeIl)}</p>
      </div>
      <CoppiaCarte
        sinistra={{ etichetta: 'Ti offre', carta: proposta.offerta }}
        destra={{ etichetta: 'Vuole', carta: proposta.richiesta }}
        mostraNome={false}
      />
      <div className="flex gap-2.5">
        <Bottone onClick={() => navigate(`/scambi/${proposta.id}`)} disabled={inPausa} className="flex-1 disabled:opacity-40">
          Accetta
        </Bottone>
        <Bottone variante="secondario" onClick={onRifiuta} className="flex-1">
          Rifiuta
        </Bottone>
      </div>
    </article>
  )
}

// 10g · Proposta che hai inviato: "Offri" ⇄ "Chiedi"
function PropostaInviata({ proposta, onAnnulla }) {
  return (
    <article className="flex flex-col gap-3">
      <div className="flex justify-between leading-[1.4]">
        <h3 className="font-semibold">A {proposta.amico.nome}</h3>
        <p className="text-didascalia font-medium text-info">⏳ In attesa · scade tra {tempoMancante(proposta.scadeIl)}</p>
      </div>
      <div className={riquadro}>
        <CoppiaCarte sinistra={{ etichetta: 'Offri', carta: proposta.offerta }} destra={{ etichetta: 'Chiedi', carta: proposta.richiesta }} />
      </div>
      <Bottone variante="distruttivo" onClick={onAnnulla}>
        Annulla proposta
      </Bottone>
    </article>
  )
}

function PropostaStorico({ proposta }) {
  return (
    <article className={riquadro}>
      <div className="flex justify-between leading-[1.4]">
        <h3 className="font-semibold">{proposta.amico.nome}</h3>
        <p className={`text-didascalia font-medium ${proposta.stato === 'accettata' ? 'text-successo' : 'text-testo-secondario'}`}>
          {etichetteStato[proposta.stato] ?? proposta.stato}
        </p>
      </div>
      <CoppiaCarte sinistra={{ etichetta: 'Offerta', carta: proposta.offerta }} destra={{ etichetta: 'Richiesta', carta: proposta.richiesta }} />
    </article>
  )
}

// 10a · Il mercato si sblocca dopo N partite
function MercatoBloccato({ partite, richieste }) {
  const tacche = Array.from({ length: richieste }, (_, i) => i)
  return (
    <>
      <section className="flex flex-col items-center gap-3 rounded-2xl border border-oro-bordo bg-sfondo px-5 py-6 text-center">
        <span aria-hidden="true" className="text-2xl">🔒</span>
        <h2 className="font-titolo text-[22px]">Il Mercato degli Echi si sblocca dopo {richieste} partite valide</h2>
        <div className="flex w-full gap-1" aria-hidden="true">
          {tacche.map((i) => (
            <span key={i} className={`h-2 flex-1 rounded ${i < partite ? 'bg-oro-bordo' : 'bg-sfondo-secondario'}`} />
          ))}
        </div>
        <p className="leading-[1.4] font-semibold">
          {partite} di {richieste} partite giocate. Ne mancano {richieste - partite}.
        </p>
      </section>
      <BottoneLink to="/gioca">Gioca una partita</BottoneLink>
    </>
  )
}
