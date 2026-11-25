import { useState } from 'react'
import AppLayout from '../components/AppLayout'
import Intestazione from '../components/Intestazione'
import Avviso from '../components/Avviso'
import Bottone, { BottoneLink } from '../components/Bottone'
import Scheletro from '../components/Scheletro'
import MessaggioErrore from '../components/MessaggioErrore'
import ProgressoLeggendaria from '../components/ProgressoLeggendaria'
import { useFetch } from '../hooks/useFetch'
import { useUtente } from '../context/UtenteContext'
import { compra, getNegozio } from '../api/negozio'

const numero = new Intl.NumberFormat('it-IT', { useGrouping: 'always' })

// 7 / D7 · /negozio
export default function Negozio() {
  const { dati, caricamento, errore, ricarica } = useFetch(getNegozio)
  const { dati: utente, ricarica: ricaricaUtente } = useUtente()
  const [acquisto, setAcquisto] = useState({ inCorso: null, esito: null })

  async function compraProdotto(prodotto) {
    setAcquisto({ inCorso: prodotto.id, esito: null })
    try {
      await compra(prodotto.id)
      setAcquisto({ inCorso: null, esito: { tipo: 'successo', testo: `Hai comprato: ${prodotto.nome}. La trovi in Collezione › Buste.` } })
      ricarica()
      ricaricaUtente() // i crediti in alto scendono
    } catch {
      setAcquisto({ inCorso: null, esito: { tipo: 'errore', testo: 'Acquisto non riuscito. Riprova.' } })
    }
  }

  const crediti = utente?.crediti

  return (
    <AppLayout paginaAttiva="negozio" sfondoDesktop="/images/sfondo-negozio-desktop.png">
      <div className="flex flex-col gap-3 lg:gap-6">
        <Intestazione titolo="Negozio" sottotitolo="Spendi i crediti che guadagni giocando." />

        {caricamento && <Scheletro className="mt-3 h-80 rounded-2xl" />}
        {errore && <MessaggioErrore />}

        {dati && (
          <div className="flex flex-col gap-3 pt-3 lg:flex-row lg:items-start lg:gap-6 lg:pt-0">
            {/* Il credito: su mobile in cima, su desktop nella colonna destra */}
            <div className="lg:hidden">
              <Credito crediti={crediti} regole={dati.regoleCrediti} />
            </div>

            <div className="flex flex-col gap-3 lg:flex-1">
              {acquisto.esito && <Avviso tipo={acquisto.esito.tipo}>{acquisto.esito.testo}</Avviso>}
              {dati.prodotti.map((prodotto) => {
                const bastano = crediti === undefined || crediti >= prodotto.prezzo
                return (
                  <article
                    key={prodotto.id}
                    className="flex items-center gap-3.5 rounded-2xl border border-oro-bordo bg-sfondo-secondario px-4 py-3.5 lg:gap-8 lg:border-oro lg:bg-sfondo lg:p-6"
                  >
                    <img src={prodotto.immagine} alt="" className="h-32 w-24 shrink-0 rounded-lg object-cover lg:h-90 lg:w-65" />
                    <div className="flex min-w-0 flex-1 flex-col gap-1.5 leading-[1.4] lg:gap-3">
                      <h2 className="font-semibold lg:font-titolo lg:text-[36px] lg:leading-normal lg:font-normal">{prodotto.nome}</h2>
                      <p className="text-didascalia font-medium text-testo-secondario lg:text-corpo lg:font-normal lg:text-testo">
                        {prodotto.descrizione}
                        {prodotto.dettaglio && <span className="hidden lg:inline"> ({prodotto.dettaglio}).</span>}
                      </p>
                      <p className="font-semibold text-testo-secondario lg:font-titolo lg:text-h2 lg:font-normal lg:text-testo">
                        {numero.format(prodotto.prezzo)} crediti
                      </p>
                      <Bottone
                        onClick={() => compraProdotto(prodotto)}
                        disabled={!bastano || acquisto.inCorso === prodotto.id}
                        className="w-full lg:w-auto lg:self-start lg:shadow-[0_6px_8px_rgba(0,0,0,0.15)]"
                      >
                        {!bastano ? 'Crediti insufficienti' : acquisto.inCorso === prodotto.id ? 'Acquisto…' : (
                          <>
                            Compra<span className="hidden lg:inline">&nbsp;per {numero.format(prodotto.prezzo)} crediti</span>
                          </>
                        )}
                      </Bottone>
                    </div>
                  </article>
                )
              })}
              <div className="lg:hidden">
                <ProgressoLeggendaria buste={dati.leggendaria.buste} totale={dati.leggendaria.totale} />
              </div>
            </div>

            {/* 💻 Desktop: colonna destra */}
            <aside className="hidden w-105 shrink-0 flex-col gap-4 lg:flex">
              <Credito crediti={crediti} regole={dati.regoleCrediti} />
              <ProgressoLeggendaria buste={dati.leggendaria.buste} totale={dati.leggendaria.totale} />
              <section className="flex flex-col gap-2.5 rounded-2xl border border-oro bg-sfondo p-6">
                <h2 className="font-titolo text-h2">Le tue buste</h2>
                <p>{dati.busteDaAprire === 0 ? 'Non hai buste da aprire.' : `Hai ${dati.busteDaAprire} buste da aprire.`}</p>
                {dati.busteDaAprire > 0 && (
                  <BottoneLink to="/collezione/buste" variante="secondario" className="min-h-12 w-full">
                    Apri le buste
                  </BottoneLink>
                )}
              </section>
            </aside>
          </div>
        )}
      </div>
    </AppLayout>
  )
}

function Credito({ crediti, regole }) {
  return (
    <section className="flex flex-col gap-2 rounded-2xl border border-oro-bordo bg-sfondo-secondario px-4 py-3.5 lg:border-oro lg:bg-sfondo lg:p-6">
      <div className="flex items-center justify-between lg:flex-col lg:items-start lg:gap-1.5">
        <h2 className="text-testo-secondario lg:leading-[1.4] lg:font-semibold lg:text-testo">Il tuo credito</h2>
        <p className="font-titolo text-[22px] lg:text-[36px]">{crediti === undefined ? '…' : `${numero.format(crediti)} crediti`}</p>
      </div>
      <p className="text-didascalia leading-[1.4] font-medium text-testo-secondario lg:text-testo">{regole}</p>
    </section>
  )
}
