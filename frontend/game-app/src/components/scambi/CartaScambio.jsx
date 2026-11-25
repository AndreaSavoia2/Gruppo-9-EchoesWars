import { coloreRarita } from '../../data/rarita'
import ImmagineCarta from '../carta/ImmagineCarta'

// Una carta dentro una proposta: etichetta sopra ("Ti offre"), immagine, nome e rarità sotto.
// compatta = true → nome e rarità sulla stessa riga ("Custode · ◆ Rara")
export default function CartaScambio({ etichetta, carta, mostraNome = true, compatta = false, allinea = 'center' }) {
  const allineamento = { center: 'items-center text-center', start: 'items-start', end: 'items-end text-right' }[allinea]
  const rarita = (
    <span className={coloreRarita[carta.rarita]}>
      <span aria-hidden="true">◆ </span>
      {carta.rarita}
    </span>
  )
  return (
    <div className={`flex flex-col gap-1.5 text-didascalia leading-[1.4] font-medium ${allineamento}`}>
      {etichetta && <span className="text-testo-secondario">{etichetta}</span>}
      <ImmagineCarta carta={carta} className="h-28 w-20 lg:h-24 lg:w-17" />
      {compatta ? (
        <span>
          {carta.nome} · {rarita}
        </span>
      ) : (
        <>
          {mostraNome && <span className="text-corpo font-semibold text-testo">{carta.nome}</span>}
          {rarita}
        </>
      )}
    </div>
  )
}
