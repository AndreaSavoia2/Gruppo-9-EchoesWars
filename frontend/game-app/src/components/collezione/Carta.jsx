import { Link } from 'react-router'
import ImmagineCarta from '../carta/ImmagineCarta'

// Carta della collezione (dal Figma "App/Carta"): immagine + pallino con le copie (×2, ×3).
// Il pallino appare solo se ne hai più di una.
export default function Carta({ carta }) {
  return (
    <Link to={`/collezione/carta/${carta.id}`} className="relative block aspect-[107/150] w-full">
      <ImmagineCarta carta={carta} className="size-full" />
      {carta.quantita > 1 && (
        <span className="absolute -top-1.5 right-1 rounded-full border-[1.5px] border-sfondo bg-testo px-2 py-0.5 text-didascalia leading-[1.4] font-medium text-sfondo lg:-top-2 lg:text-[16.8px]">
          ×{carta.quantita}
          <span className="sr-only"> copie</span>
        </span>
      )}
    </Link>
  )
}
