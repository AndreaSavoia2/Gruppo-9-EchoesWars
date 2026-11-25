import { Link } from 'react-router'
import { useUtente } from '../context/UtenteContext'
import Scheletro from './Scheletro'

// Formatta i numeri all'italiana col punto: 1250 → "1.250"
// (useGrouping: 'always' serve perché in italiano, di base, 1250 resta senza punto)
const formatoCrediti = new Intl.NumberFormat('it-IT', { useGrouping: 'always' })

// Barra in alto: avatar, nome utente e crediti.
// Nome e crediti arrivano dal backend (useUtente).
export default function TopBar() {
  const { dati: utente, caricamento } = useUtente()

  return (
    <header className="sticky top-0 z-10 flex h-18 items-center gap-3 border border-oro-bordo bg-sfondo-secondario px-5">
      {/* Avatar: cerchio con icona, porta al profilo */}
      <Link
        to="/profilo"
        aria-label="Il tuo profilo"
        className="flex size-11 shrink-0 items-center justify-center rounded-full border-2 border-testo"
      >
        {utente?.avatar ? (
          <img src={utente.avatar} alt="" className="size-8 object-contain" />
        ) : (
          <img src="/icons/avatar.svg" alt="" className="size-6" />
        )}
      </Link>

      {/* flex-1 = "prendi tutto lo spazio libero" → spinge i crediti a destra */}
      <div className="min-w-0 flex-1">
        {caricamento ? (
          <Scheletro className="h-4 w-28" />
        ) : (
          <p className="truncate font-medium">{utente?.nome}</p>
        )}
      </div>

      {/* Crediti: li mostriamo solo quando sono arrivati */}
      {caricamento ? (
        <Scheletro className="h-11 w-24 rounded-full" />
      ) : (
        utente && (
          <div className="flex min-h-11 shrink-0 items-center gap-1.5 rounded-full border border-testo bg-sfondo-secondario px-3.5">
            <img src="/icons/crediti.svg" alt="" className="size-4" />
            <span className="text-didascalia font-semibold">
              {formatoCrediti.format(utente.crediti)}
              <span className="sr-only"> crediti</span>
            </span>
          </div>
        )
      )}
    </header>
  )
}
