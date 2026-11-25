import { useState } from 'react'
import { Link, useNavigate } from 'react-router'
import AppLayout from '../components/AppLayout'
import Intestazione from '../components/Intestazione'
import Bottone from '../components/Bottone'
import { echi } from '../data/echi'
import { REGOLE_MAZZO } from '../data/regole'

// 5q · 5r · /collezione/mazzi/nuovo — Passo 1: scegli i 2 Echi
export default function NuovoMazzo() {
  const navigate = useNavigate()
  const [scelti, setScelti] = useState([])
  const completo = scelti.length === REGOLE_MAZZO.echi
  const mancano = REGOLE_MAZZO.echi - scelti.length

  function tocca(nome) {
    if (scelti.includes(nome)) setScelti(scelti.filter((n) => n !== nome))
    else if (!completo) setScelti([...scelti, nome])
  }

  function continua() {
    // Le Eco scelte viaggiano nell'indirizzo: /collezione/mazzi/nuovo/carte?eco=Sapienza&eco=Oblio
    const parametri = new URLSearchParams(scelti.map((eco) => ['eco', eco]))
    navigate(`/collezione/mazzi/nuovo/carte?${parametri}`)
  }

  return (
    <AppLayout paginaAttiva="collezione" sfondoMobile="/images/sfondo-mazzo.png" sfondoDesktop="/images/sfondo-mazzo-desktop.png">
      <div className="flex flex-col gap-3 lg:max-w-3xl lg:gap-5">
        <Intestazione titolo="Nuovo mazzo" sottotitolo="Passo 1 di 2 · Scegli 2 Echi" />
        <Link to="/collezione/mazzi" className="mt-3 self-start font-medium underline lg:mt-0">
          ‹ Mazzi
        </Link>
        <p>Un mazzo usa solo carte di 2 Echi. Tocca due riquadri: potrai cambiarli finché non salvi.</p>

        <ul className="grid grid-cols-2 gap-5 lg:grid-cols-4">
          {echi.map((eco) => {
            const selezionato = scelti.includes(eco.nome)
            const disattivato = completo && !selezionato
            return (
              <li key={eco.nome}>
                <button
                  type="button"
                  onClick={() => tocca(eco.nome)}
                  disabled={disattivato}
                  aria-pressed={selezionato}
                  className={`flex aspect-square w-full flex-col items-center justify-center gap-1.5 rounded-[18px] leading-[1.4] ${
                    selezionato ? 'border-3 border-testo bg-sfondo-secondario' : 'border border-oro bg-sfondo'
                  } ${disattivato ? 'cursor-not-allowed opacity-45' : ''}`}
                >
                  <span className={`h-1 w-10 rounded-sm ${eco.sfondo}`} />
                  <img src={eco.icona} alt="" className="size-18 object-contain" />
                  <span className="font-semibold">{eco.nome}</span>
                  <span className="text-didascalia font-medium">
                    {selezionato ? '✓ Scelto' : disattivato ? `Hai già ${REGOLE_MAZZO.echi} Echi` : eco.descrizione}
                  </span>
                </button>
              </li>
            )
          })}
        </ul>

        <p className="leading-[1.4] font-semibold" aria-live="polite">
          {scelti.length} di {REGOLE_MAZZO.echi} Echi scelti{scelti.length > 0 && `: ${scelti.join(' + ')}`}
        </p>
        <Bottone onClick={continua} disabled={!completo} className="w-full lg:w-auto lg:min-w-60 lg:self-start">
          {completo ? 'Continua' : `Scegli ancora ${mancano} ${mancano === 1 ? 'Eco' : 'Echi'}`}
        </Bottone>
      </div>
    </AppLayout>
  )
}
