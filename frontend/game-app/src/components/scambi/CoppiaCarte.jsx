import CartaScambio from './CartaScambio'

/**
 * Le due carte di uno scambio, con il simbolo in mezzo (⇄ oppure ✓ a scambio fatto)
 *
 * @param {object} props - le proprietà del componente
 * @param {*} props.sinistra
 * @param {*} props.destra
 * @param {string} [props.simbolo='⇄']
 * @param {object} [props.opzioni] - tutte le altre proprietà
 * @returns {JSX.Element}
 */
export default function CoppiaCarte({ sinistra, destra, simbolo = '⇄', ...opzioni }) {
  return (
    <div className="flex items-center justify-between">
      <CartaScambio {...sinistra} {...opzioni} />
      <span aria-hidden="true" className={`font-titolo text-[22px] ${simbolo === '✓' ? 'text-[40px] text-successo' : 'text-testo-secondario'}`}>
        {simbolo}
      </span>
      <CartaScambio {...destra} {...opzioni} />
    </div>
  )
}
