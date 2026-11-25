import ImmagineCarta from '../carta/ImmagineCarta'

/**
 * Spazio tratteggiato dove sta la carta giocata, con la DEF in basso a destra.
 * carta = null → spazio vuoto
 *
 * @param {object} props - le proprietà del componente
 * @param {*} props.carta
 * @returns {JSX.Element}
 */
export default function CartaInCampo({ carta }) {
  return (
    <div className="relative flex h-46 w-33 items-center justify-center rounded-[10px] border-2 border-dashed border-oro-bordo bg-sfondo/25">
      {carta && (
        <>
          <ImmagineCarta carta={carta} className="h-43.5 w-31" />
          <span className="absolute right-0 -bottom-2 rounded-full bg-testo px-2 py-0.5 text-didascalia leading-[1.4] font-medium text-sfondo">
            DEF {carta.def}
          </span>
        </>
      )}
    </div>
  )
}
