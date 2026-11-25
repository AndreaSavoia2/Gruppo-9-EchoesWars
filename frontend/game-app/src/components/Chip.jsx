import { Link } from 'react-router'

/**
 * Bottoncino arrotondato (Richieste, Missioni, Cronologia).
 * notifica = true → mostra il pallino viola in alto a destra.
 *
 * @param {object} props - le proprietà del componente
 * @param {string} props.testo
 * @param {string} props.percorso
 * @param {boolean} [props.notifica=false]
 * @returns {JSX.Element}
 */
export default function Chip({ testo, percorso, notifica = false }) {
  return (
    <Link
      to={percorso}
      className="relative flex min-h-11 items-center justify-center rounded-full border border-oro-bordo bg-oro px-4.5 font-medium hover:brightness-95"
    >
      {testo}
      {notifica && (
        <>
          <img src="/icons/puntino-notifica.svg" alt="" className="absolute -top-1.5 right-0 size-3.5" />
          <span className="sr-only"> (novità)</span>
        </>
      )}
    </Link>
  )
}
