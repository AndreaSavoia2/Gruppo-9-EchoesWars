import Confronto from './Confronto'
import { iniziali } from '../../utils/testo'

/**
 * G4 / DG3 · "Stiamo simulando la partita": TU contro l'avversario.
 *
 * @param {object} props - le proprietà del componente
 * @param {*} props.partita
 * @returns {JSX.Element}
 */
export default function Simulazione({ partita }) {
  return (
    <div className="flex flex-col gap-6" aria-live="polite">
      <div className="flex flex-col gap-2.5 text-center lg:gap-1.5 lg:text-left">
        <h1 className="font-titolo text-[22px] lg:text-h2">Stiamo simulando la partita</h1>
        <p>
          {partita.mioMazzo} contro il mazzo di {partita.avversario.nome}. Ci vogliono pochi secondi: non devi fare
          nulla.
          <span className="hidden lg:inline"> Il risultato arriva anche se chiudi l’app.</span>
        </p>
      </div>

      <div className="mt-16 lg:mt-0">
        <Confronto inizialiAvversario={iniziali(partita.avversario.nome)} />
      </div>
    </div>
  )
}
