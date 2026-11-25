import { Link } from 'react-router'

// Schede a "pillola" (Carte · Mazzi · Buste, Ricevute · Inviate · Storico...).
// schede: [{ id, etichetta, percorso }] · attiva: id della scheda selezionata
export default function Schede({ schede, attiva, etichetta, className = 'lg:w-105' }) {
  return (
    <nav aria-label={etichetta} className={`flex w-full rounded-full bg-sfondo-secondario p-1 ${className}`}>
      {schede.map((scheda) => {
        const selezionata = scheda.id === attiva
        return (
          <Link
            key={scheda.id}
            to={scheda.percorso}
            aria-current={selezionata ? 'page' : undefined}
            className={`flex min-h-11 flex-1 items-center justify-center rounded-full font-medium ${
              selezionata ? 'border-[1.5px] border-testo bg-oro' : 'hover:bg-sfondo'
            }`}
          >
            {scheda.etichetta}
          </Link>
        )
      })}
    </nav>
  )
}
