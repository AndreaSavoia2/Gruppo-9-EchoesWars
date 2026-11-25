import { useCallback, useState } from 'react'
import AppLayout from '../components/AppLayout'
import Intestazione from '../components/Intestazione'
import Bottone, { BottoneLink } from '../components/Bottone'
import Modale from '../components/Modale'
import RigaLink from '../components/RigaLink'
import Scheletro from '../components/Scheletro'
import MessaggioErrore from '../components/MessaggioErrore'
import { useFetch } from '../hooks/useFetch'
import { esci, getProfilo } from '../api/profilo'
import { vaiAlLogin } from '../api/token'

// 11 / D11 · /profilo
export default function Profilo() {
  const { dati: profilo, caricamento, errore } = useFetch(getProfilo)
  const [confermaEsci, setConfermaEsci] = useState(false)
  const chiudi = useCallback(() => setConfermaEsci(false), [])

  async function esciDallAccount() {
    await esci() // avvisa il backend e cancella il token
    // Torna al login statico (o, se non è configurato, alla Home dell'app)
    if (!vaiAlLogin('uscito')) window.location.href = '/'
  }

  const accessi = profilo?.accessi
  // Ogni voce: su quali schermi appare (dal Figma, mobile e desktop hanno voci un po' diverse)
  const voci = profilo
    ? [
        { titolo: 'Modifica avatar', dettaglio: 'L’immagine accanto al tuo nome', icona: '/icons/matita.svg', percorso: '/profilo/avatar' },
        { titolo: 'Modifica dati', dettaglio: 'Nome utente, email e password', icona: '/icons/matita.svg', percorso: '/profilo/dati' },
        { titolo: 'Cronologia partite', dettaglio: `${profilo.partite} partite giocate`, icona: '/icons/orologio.svg', percorso: '/cronologia', solo: 'desktop' },
        { titolo: 'Lingua', dettaglio: profilo.lingua, icona: '/icons/globo.svg', percorso: '/profilo/lingua' },
        {
          titolo: 'Accessi consecutivi',
          // ⚠️ Gli accessi consecutivi non ci sono ancora nel backend (accessi = null)
          dettaglio: accessi
            ? `Giorno ${accessi.giorno} di ${accessi.totale} · prossima ricompensa al ${accessi.prossimaRicompensa.giorno}`
            : 'Presto disponibile',
          icona: '/icons/orologio.svg',
          percorso: '/accessi',
          solo: 'mobile',
        },
        { titolo: 'Cambia password', dettaglio: 'Serve la password attuale', icona: '/icons/matita.svg', percorso: '/profilo/password', solo: 'desktop' },
      ]
    : []
  const visibilita = { mobile: 'lg:hidden', desktop: 'hidden lg:block' }

  return (
    <AppLayout sfondoMobile={null}>
      <div className="flex flex-col gap-2.5 lg:gap-6">
        <Intestazione titolo="Profilo" sottotitolo={profilo ? `${profilo.nome} · ${profilo.email}` : ' '} />

        {caricamento && <Scheletro className="mt-3 h-80 rounded-2xl" />}
        {errore && <MessaggioErrore />}

        {profilo && (
          <div className="flex flex-col gap-2.5 pt-3 lg:flex-row lg:items-start lg:gap-6 lg:pt-0">
            <div className="flex flex-col gap-4 lg:flex-1">
              <dl className="flex gap-2.5 lg:gap-1">
                {[
                  ['Partite', profilo.partite],
                  ['Vittorie', profilo.vittorie],
                  ['Carte', profilo.carte],
                ].map(([nome, valore]) => (
                  <div key={nome} className="flex flex-1 flex-col-reverse items-center gap-0.5 rounded-[14px] bg-sfondo-secondario py-3">
                    <dt className="text-didascalia leading-[1.4] font-medium text-testo-secondario">{nome}</dt>
                    <dd className="font-titolo text-[22px] lg:text-h2">{valore}</dd>
                  </div>
                ))}
              </dl>

              {/* 💻 Desktop: riquadro Accessi consecutivi */}
              <section className="hidden flex-col items-start gap-2.5 rounded-2xl border border-oro bg-sfondo p-6 lg:flex">
                <h2 className="font-titolo text-h2">Accessi consecutivi</h2>
                {accessi ? (
                  <p>
                    Sei al giorno {accessi.giorno} di {accessi.totale}. Prossima ricompensa al giorno{' '}
                    {accessi.prossimaRicompensa.giorno}: {accessi.prossimaRicompensa.nome}.
                  </p>
                ) : (
                  <p>Presto disponibile.</p>
                )}
                <BottoneLink to="/accessi" variante="secondario" className="min-h-12 w-auto">
                  Vedi le ricompense
                </BottoneLink>
              </section>
            </div>

            <section className="flex flex-col gap-2.5 lg:w-100 lg:shrink-0 lg:gap-3">
              <h2 className="hidden font-titolo text-h2 lg:block">Impostazioni</h2>
              <ul className="flex flex-col gap-2.5 lg:gap-3">
                {voci.map((voce) => (
                  <li key={voce.titolo} className={visibilita[voce.solo] ?? ''}>
                    <RigaLink {...voce} />
                  </li>
                ))}
              </ul>
              <Bottone variante="distruttivo" onClick={() => setConfermaEsci(true)} className="w-full lg:min-h-12">
                Esci dall’account
              </Bottone>
            </section>
          </div>
        )}
      </div>

      {confermaEsci && (
        <Modale titolo="Uscire dall’account?" onChiudi={chiudi}>
          <p className="text-testo-secondario">Per giocare di nuovo dovrai accedere con email e password.</p>
          <Bottone variante="distruttivo" onClick={esciDallAccount}>
            Esci
          </Bottone>
          <Bottone variante="secondario" onClick={chiudi}>
            Annulla
          </Bottone>
        </Modale>
      )}
    </AppLayout>
  )
}
