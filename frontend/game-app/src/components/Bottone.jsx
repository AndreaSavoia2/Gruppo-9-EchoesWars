import { Link } from 'react-router'

// Stili dei bottoni dell'app (dal Figma "App/Bottone"), alti almeno 44px.
//   primario   → oro pieno: l'azione principale (una sola per schermata)
//   secondario → solo bordo: alternativa / annulla
//   distruttivo → bordo e testo rossi: azione che non si può annullare (sempre con conferma!)
const varianti = {
  primario: 'bg-oro-bordo hover:brightness-95',
  secondario: 'border-[1.5px] border-testo hover:bg-sfondo-secondario',
  distruttivo: 'border-[1.5px] border-errore text-errore hover:bg-errore-sfondo',
}
const base =
  'flex min-h-11 items-center justify-center rounded-full px-5 font-medium disabled:cursor-not-allowed disabled:opacity-50'

// <Bottone> = un vero <button> (per azioni: inviare, confermare...)
export default function Bottone({ variante = 'primario', className = 'w-full', children, ...altreProps }) {
  return (
    <button className={`${base} ${varianti[variante]} ${className}`} {...altreProps}>
      {children}
    </button>
  )
}

// <BottoneLink> = sembra un bottone ma porta a un'altra pagina
export function BottoneLink({ variante = 'primario', className = 'w-full', children, ...altreProps }) {
  return (
    <Link className={`${base} ${varianti[variante]} ${className}`} {...altreProps}>
      {children}
    </Link>
  )
}
