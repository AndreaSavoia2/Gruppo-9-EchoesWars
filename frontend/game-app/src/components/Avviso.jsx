// Riquadro di avviso colorato (dal Figma). tipo: info · errore · successo
const stili = {
  info: { box: 'border-info bg-info-sfondo', icona: 'i', colore: 'text-info' },
  errore: { box: 'border-errore bg-errore-sfondo', icona: '⚠', colore: 'text-errore' },
  successo: { box: 'border-successo bg-successo-sfondo', icona: '✓', colore: 'text-successo' },
}

/**
 * barra = true → bordo spesso solo a sinistra (come negli Scambi)
 *
 * @param {object} props - le proprietà del componente
 * @param {string} [props.tipo='info']
 * @param {string} props.icona
 * @param {boolean} [props.barra=false]
 * @param {React.ReactNode} props.children
 * @returns {JSX.Element}
 */
export default function Avviso({ tipo = 'info', icona, barra = false, children }) {
  const stile = stili[tipo]
  return (
    <p
      role={tipo === 'errore' ? 'alert' : undefined}
      className={`flex items-center gap-2.5 rounded-xl px-3.5 py-2.5 text-didascalia leading-[1.4] font-medium ${stile.box} ${
        barra ? 'border-l-4' : 'border'
      }`}
    >
      <span aria-hidden="true" className={`text-corpo font-semibold ${stile.colore}`}>
        {icona ?? stile.icona}
      </span>
      <span className="flex-1">{children}</span>
    </p>
  )
}
