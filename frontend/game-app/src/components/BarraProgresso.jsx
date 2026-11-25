// Barra di avanzamento (dal Figma "App/Barra progresso") con il valore scritto accanto (es. 1/2).
export default function BarraProgresso({ fatto, totale }) {
  const percentuale = Math.min(100, (fatto / totale) * 100)
  return (
    <div className="flex items-center gap-2">
      <div
        role="progressbar"
        aria-valuenow={fatto}
        aria-valuemin={0}
        aria-valuemax={totale}
        className="h-2 flex-1 overflow-hidden rounded border border-testo-secondario bg-sfondo-secondario"
      >
        <div className="h-full bg-oro-bordo" style={{ width: `${percentuale}%` }} />
      </div>
      <span className="text-didascalia leading-[1.4] font-medium">
        {fatto}/{totale}
      </span>
    </div>
  )
}
