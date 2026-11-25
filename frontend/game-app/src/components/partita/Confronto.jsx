/**
 * Due cerchi "TU" contro l'avversario (Simulazione, Attesa sfida, Sfida accettata).
 * centro:            '• • •' oppure 'VS'
 * conOnde:           su mobile mostra le onde intorno all'avversario (si sta aspettando)
 * avversarioSbiadito: l'avversario è più chiaro (non ha ancora risposto / sta giocando)
 *
 * @param {object} props - le proprietà del componente
 * @param {*} props.inizialiAvversario
 * @param {string} [props.centro='• • •']
 * @param {boolean} [props.conOnde=true]
 * @param {boolean} [props.avversarioSbiadito=true]
 * @returns {JSX.Element}
 */
export default function Confronto({ inizialiAvversario, centro = '• • •', conOnde = true, avversarioSbiadito = true }) {
  const vs = centro === 'VS'
  return (
    <div className="flex items-center justify-between px-5 lg:justify-center lg:gap-10 lg:py-10">
      <Avatar testo="TU" />
      <span
        aria-hidden={!vs}
        className={`font-titolo lg:text-[36px] lg:text-testo lg:opacity-100 ${
          vs ? 'text-[44px] text-testo-secondario' : 'text-[28px] opacity-45'
        }`}
      >
        {centro}
      </span>
      <div className="relative">
        {conOnde && (
          <span aria-hidden="true" className="lg:hidden">
            <img src="/icons/onde/onda-3.svg" alt="" className="absolute top-1/2 left-1/2 size-42.5 max-w-none -translate-1/2 motion-safe:animate-pulse" />
            <img src="/icons/onde/onda-2.svg" alt="" className="absolute top-1/2 left-1/2 size-35 max-w-none -translate-1/2" />
            <img src="/icons/onde/onda-1.svg" alt="" className="absolute top-1/2 left-1/2 size-27.5 max-w-none -translate-1/2" />
          </span>
        )}
        <Avatar testo={inizialiAvversario} sbiadito={avversarioSbiadito} />
      </div>
    </div>
  )
}

/**
 * Cerchio con le iniziali di un giocatore (sbiadito = ancora in attesa).
 *
 * @param {object} props - le proprietà del componente
 * @param {string} props.testo
 * @param {boolean} [props.sbiadito=false]
 * @returns {JSX.Element}
 */
function Avatar({ testo, sbiadito = false }) {
  return (
    <div
      className={`relative flex size-24 items-center justify-center rounded-full border-3 border-oro-bordo bg-oro font-titolo text-[34px] lg:size-35 lg:border-oro lg:bg-sfondo-secondario lg:text-[28px] lg:opacity-100 ${
        sbiadito ? 'opacity-55' : ''
      }`}
    >
      {testo}
    </div>
  )
}
