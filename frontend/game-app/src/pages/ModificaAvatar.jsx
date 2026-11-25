import { useState } from 'react'
import { Link, useNavigate } from 'react-router'
import AppLayout from '../components/AppLayout'
import Intestazione from '../components/Intestazione'
import Avviso from '../components/Avviso'
import Bottone from '../components/Bottone'
import Scheletro from '../components/Scheletro'
import MessaggioErrore from '../components/MessaggioErrore'
import { useFetch } from '../hooks/useFetch'
import { useUtente } from '../context/UtenteContext'
import { getAvatar, salvaAvatar } from '../api/profilo'

/**
 * 11b / D11b · /profilo/avatar
 *
 * @returns {JSX.Element}
 */
export default function ModificaAvatar() {
  const navigate = useNavigate()
  const { dati: avatar, caricamento, errore } = useFetch(getAvatar)
  const { ricarica: ricaricaUtente } = useUtente()
  const [sceltoId, setSceltoId] = useState(null) // null = non hai ancora toccato niente
  const [salvataggio, setSalvataggio] = useState({ inCorso: false, errore: null })

  const inUso = avatar?.find((a) => a.inUso)?.id
  const scelto = sceltoId ?? inUso

  async function salva() {
    setSalvataggio({ inCorso: true, errore: null })
    try {
      await salvaAvatar(scelto)
      ricaricaUtente() // l'avatar nuovo compare subito nella barra in alto
      navigate('/profilo')
    } catch {
      setSalvataggio({ inCorso: false, errore: 'Salvataggio non riuscito. Riprova.' })
    }
  }

  return (
    <AppLayout sfondoMobile={null}>
      <div className="flex flex-col gap-2.5 lg:gap-4">
        <Intestazione titolo="Modifica avatar" sottotitolo="Scegli l’immagine che vedi in alto, accanto al tuo nome." />
        <Link to="/profilo" className="mt-3 self-start font-medium underline lg:mt-0">
          ‹ Profilo
        </Link>

        {caricamento && <Scheletro className="h-80 rounded-2xl" />}
        {errore && <MessaggioErrore />}

        {avatar && (
          <>
            <ul className="grid grid-cols-3 gap-x-7.75 gap-y-5 lg:grid-cols-[repeat(auto-fill,100px)] lg:gap-x-6">
              {avatar.map((voce) => {
                const selezionato = voce.id === scelto
                return (
                  <li key={voce.id}>
                    <button
                      type="button"
                      onClick={() => setSceltoId(voce.id)}
                      disabled={!voce.disponibile}
                      aria-pressed={selezionato}
                      className="flex w-full flex-col items-center gap-1.5 text-center leading-[1.4] disabled:cursor-not-allowed"
                    >
                      <span
                        className={`relative flex size-22 items-center justify-center rounded-full ${
                          selezionato ? 'border-4 border-testo bg-oro' : 'border-[1.5px] border-oro-bordo'
                        } ${voce.disponibile ? 'bg-oro' : 'bg-sfondo-secondario'}`}
                      >
                        {voce.immagine && voce.disponibile && <img src={voce.immagine} alt="" className="size-16 object-contain" />}
                        {!voce.disponibile && (
                          <span aria-hidden="true" className="flex size-16 items-center justify-center bg-oro/35 font-titolo text-[22px]">
                            🔒
                          </span>
                        )}
                        {selezionato && (
                          <span aria-hidden="true" className="absolute -right-1 -bottom-1 flex size-7 items-center justify-center rounded-full bg-testo font-semibold text-sfondo">
                            ✓
                          </span>
                        )}
                      </span>
                      <span className="font-semibold">{voce.nome}</span>
                      <span className="text-didascalia font-medium">
                        {selezionato ? '✓ In uso' : voce.disponibile ? 'Disponibile' : voce.sblocco}
                      </span>
                    </button>
                  </li>
                )
              })}
            </ul>

            <Avviso tipo="info" icona="🔒">
              Gli avatar bloccati si sbloccano con gli accessi consecutivi e le Prove degli Echi.
            </Avviso>
            <Bottone onClick={salva} disabled={scelto === inUso || salvataggio.inCorso} className="w-full lg:min-h-12 lg:w-auto lg:min-w-35 lg:self-start">
              {salvataggio.inCorso ? 'Salvataggio…' : 'Salva avatar'}
            </Bottone>
            {salvataggio.errore && <MessaggioErrore testo={salvataggio.errore} />}
          </>
        )}
      </div>
    </AppLayout>
  )
}
