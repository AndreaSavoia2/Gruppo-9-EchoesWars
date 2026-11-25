import CartaInCampo from './CartaInCampo'
import ImmagineCarta from '../carta/ImmagineCarta'

/**
 * Un lato del campo di battaglia (dal Figma "Battaglia/Lato giocatore"):
 * pillola con nome + PV, e sotto: mazzo · carta in campo · scarti.
 * inAlto = true per l'avversario (la pillola va sopra il campo)
 *
 * @param {object} props - le proprietà del componente
 * @param {string} props.nome
 * @param {*} props.stato
 * @param {*} props.inAlto
 * @param {*} props.nascondiCarta
 * @returns {JSX.Element}
 */
export default function LatoGiocatore({ nome, stato, inAlto, nascondiCarta }) {
  const pillola = (
    <p className="flex items-center gap-2.5 rounded-full border-[1.5px] border-oro-bordo bg-oro px-3.5 py-2 leading-[1.4] font-semibold">
      {nome}
      <span className="flex items-center gap-1 rounded-full bg-errore px-2 py-0.5 text-sfondo">
        <span aria-hidden="true" className="text-didascalia font-medium">♥</span>
        {stato.pv} PV
      </span>
    </p>
  )

  return (
    <section aria-label={nome} className="flex flex-col items-center gap-3 px-4 py-2">
      {inAlto && pillola}

      <div className="flex items-center justify-center gap-4.5">
        <Pila etichetta="Mazzo" numero={stato.carteMazzo}>
          {[6, 3, 0].map((spostamento) => (
            <span
              key={spostamento}
              style={{ top: spostamento, left: spostamento }}
              className="absolute flex h-24.5 w-17.5 items-center justify-center rounded-md border-[1.5px] border-oro-bordo bg-sfondo-secondario"
            >
              <img src="/icons/stella.png" alt="" className="size-7.5 object-contain" />
            </span>
          ))}
        </Pila>

        <CartaInCampo carta={nascondiCarta ? null : stato.carta} />

        <Pila etichetta="Scarti" numero={stato.carteScarti}>
          {stato.ultimaScartata && (
            <>
              {[
                [6, 'opacity-50'],
                [3, 'opacity-50'],
                [0, 'opacity-85'],
              ].map(([spostamento, opacita]) => (
                <div key={spostamento} style={{ top: spostamento, left: spostamento }} className={`absolute ${opacita}`}>
                  <ImmagineCarta carta={stato.ultimaScartata} className="h-24.5 w-17.5" />
                </div>
              ))}
              <span className="absolute top-0 left-0 h-24.5 w-17.5 rounded-md bg-[rgba(33,38,43,0.35)]" />
            </>
          )}
        </Pila>
      </div>

      {!inAlto && pillola}
    </section>
  )
}

/**
 * Pila di carte con il numero in un pallino in alto a destra
 *
 * @param {object} props - le proprietà del componente
 * @param {string} props.etichetta
 * @param {*} props.numero
 * @param {React.ReactNode} props.children
 * @returns {JSX.Element}
 */
function Pila({ etichetta, numero, children }) {
  return (
    <div className="relative h-26.5 w-19">
      {children}
      <span className="absolute top-0 right-0 rounded-full border-[1.5px] border-sfondo bg-testo px-2 py-0.5 text-didascalia leading-[1.4] font-medium text-sfondo">
        <span className="sr-only">{etichetta}: </span>
        {numero}
      </span>
    </div>
  )
}
