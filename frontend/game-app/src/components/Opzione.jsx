import { Link } from 'react-router'

// Riga cliccabile con icona, titolo, descrizione e freccia (versione mobile).
export default function Opzione({ percorso, icona, titolo, descrizione }) {
  return (
    <Link
      to={percorso}
      className="flex w-full items-center gap-3.5 rounded-2xl border border-oro-bordo bg-sfondo px-4 py-3.5 hover:bg-sfondo-secondario"
    >
      <span className="flex size-11 shrink-0 items-center justify-center rounded-xl bg-oro">
        <img src={icona} alt="" className="size-6" />
      </span>
      <span className="flex min-w-0 flex-1 flex-col gap-0.5 leading-[1.4]">
        <span className="font-semibold">{titolo}</span>
        <span className="text-didascalia font-medium text-testo-secondario">{descrizione}</span>
      </span>
      <img src="/icons/freccia-destra.svg" alt="" className="size-6 shrink-0" />
    </Link>
  )
}
