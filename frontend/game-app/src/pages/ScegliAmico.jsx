import { useState } from 'react'
import { Link, useNavigate } from 'react-router'
import AppLayout from '../components/AppLayout'
import Intestazione from '../components/Intestazione'
import Bottone from '../components/Bottone'
import RigaSelezionabile from '../components/RigaSelezionabile'
import Scheletro from '../components/Scheletro'
import MessaggioErrore from '../components/MessaggioErrore'
import { useFetch } from '../hooks/useFetch'
import { getAmici, inviaSfida } from '../api/sfide'
import { tempoFa } from '../utils/testo'

// A1 / DA1 · /gioca/amico — scegli quale amico sfidare
export default function ScegliAmico() {
  const navigate = useNavigate()
  const { dati: amici, caricamento, errore } = useFetch(getAmici)
  const [idScelto, setIdScelto] = useState(null)
  const [invio, setInvio] = useState({ inCorso: false, errore: null })

  const amicoScelto = amici?.find((a) => a.id === idScelto)

  async function invia() {
    setInvio({ inCorso: true, errore: null })
    try {
      const sfida = await inviaSfida(amicoScelto.id)
      // Richiesta inviata: ora scegli il mazzo mentre aspetti
      navigate(`/sfida/${sfida.id}/mazzo`)
    } catch {
      setInvio({ inCorso: false, errore: 'Non è stato possibile inviare la richiesta. Riprova.' })
    }
  }

  return (
    <AppLayout paginaAttiva="gioca" sfondoMobile="/images/sfondo-gioca.png">
      <div className="flex flex-col gap-5 lg:gap-6">
        <Intestazione titolo="Sfida un amico" sottotitolo="Scegli chi sfidare.">
          <Link to="/gioca" className="hidden font-medium underline lg:block">
            ‹ Come vuoi giocare?
          </Link>
        </Intestazione>

        <section className="flex flex-col gap-2.5 lg:rounded-2xl lg:border lg:border-oro lg:bg-sfondo lg:p-6">
          <h2 className="leading-[1.4] font-semibold lg:sr-only">I tuoi amici</h2>

          {caricamento && <Scheletro className="h-72 rounded-2xl" />}
          {errore && <MessaggioErrore />}
          {amici?.length === 0 && <p>Non hai ancora amici da sfidare. Aggiungili dalla sezione Amici.</p>}

          {amici?.length > 0 && (
            <ul className="flex flex-col gap-2.5 lg:grid lg:grid-cols-[repeat(auto-fill,minmax(360px,500px))] lg:gap-3">
              {amici.map((amico) => (
                <li key={amico.id}>
                  <RigaSelezionabile
                    titolo={amico.nome}
                    dettaglio={amico.ultimaSfida ? `Ultima sfida: ${tempoFa(amico.ultimaSfida)}` : 'Mai sfidato'}
                    iconaDesktop="/icons/spade.svg"
                    scelto={amico.id === idScelto}
                    onScegli={() => setIdScelto(amico.id)}
                  />
                </li>
              ))}
            </ul>
          )}
        </section>

        <Bottone onClick={invia} disabled={!amicoScelto || invio.inCorso} className="w-full lg:min-h-12 lg:w-auto lg:self-start">
          {invio.inCorso ? 'Invio…' : amicoScelto ? `Invia la richiesta a ${amicoScelto.nome}` : 'Scegli un amico'}
        </Bottone>
        {invio.errore && <MessaggioErrore testo={invio.errore} />}
      </div>
    </AppLayout>
  )
}
