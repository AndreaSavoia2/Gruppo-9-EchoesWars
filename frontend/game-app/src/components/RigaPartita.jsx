import { Link } from 'react-router'

// Una riga della cronologia: esito (icona + parola) + avversario + round.
// percorso (facoltativo): la riga diventa un link (es. ai dettagli della partita)
const stili = {
  vittoria: {
    box: 'bg-successo-sfondo border-successo text-successo',
    icona: '/icons/vittoria.svg',
    testo: 'Vittoria',
  },
  sconfitta: {
    box: 'bg-errore-sfondo border-errore text-errore',
    icona: '/icons/sconfitta.svg',
    testo: 'Sconfitta',
  },
  pareggio: {
    box: 'bg-info-sfondo border-info text-info',
    icona: '/icons/pareggio.svg',
    testo: 'Pareggio',
  },
}

export default function RigaPartita({ esito, avversario, round, percorso, conVs = false }) {
  const stile = stili[esito]

  const Contenitore = percorso ? Link : 'div'
  return (
    <Contenitore to={percorso} className="flex min-h-18 w-full items-center gap-3 rounded-2xl border border-oro-bordo bg-sfondo p-3">
      <div
        className={`flex h-14 w-18 shrink-0 flex-col items-center justify-center gap-0.5 rounded-xl border-[1.5px] ${stile.box}`}
      >
        <img src={stile.icona} alt="" className="size-5" />
        <span className="text-didascalia leading-[1.4] font-medium">{stile.testo}</span>
      </div>
      <div className="flex min-w-0 flex-col gap-0.5 leading-[1.4]">
        <p className="truncate font-semibold">{conVs ? `vs ${avversario}` : avversario}</p>
        <p className="text-didascalia font-medium">{round} round</p>
      </div>
    </Contenitore>
  )
}
