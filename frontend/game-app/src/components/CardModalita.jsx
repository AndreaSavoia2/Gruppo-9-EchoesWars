import { Link } from 'react-router'

/**
 * Card grande per scegliere la modalità di gioco (versione desktop):
 * immagine, icona + titolo, descrizione, 3 passi numerati, bottone.
 *
 * @param {object} props - le proprietà del componente
 * @param {string} props.percorso
 * @param {*} props.immagine
 * @param {string} props.icona
 * @param {string} props.titolo
 * @param {*} props.descrizione
 * @param {*} props.passi
 * @param {*} props.testoBottone
 * @returns {JSX.Element}
 */
export default function CardModalita({ percorso, immagine, icona, titolo, descrizione, passi, testoBottone }) {
  return (
    <Link
      to={percorso}
      className="group flex flex-col overflow-hidden rounded-2xl border border-oro-bordo bg-sfondo"
    >
      <img src={immagine} alt="" className="h-55 w-full object-cover" />

      <div className="flex flex-1 flex-col gap-4.5 p-7">
        <div className="flex items-center gap-3.5">
          <span className="flex size-13 shrink-0 items-center justify-center rounded-xl bg-oro">
            <img src={icona} alt="" className="size-6" />
          </span>
          <h2 className="font-titolo text-[36px]">{titolo}</h2>
        </div>

        <p className="text-testo-secondario">{descrizione}</p>

        <hr className="border-oro-bordo/50" />

        <p className="leading-[1.4] font-semibold">Come funziona</p>

        {/* <ol> = lista numerata: i numeri li disegniamo noi nei cerchietti */}
        <ol className="flex flex-col gap-3">
          {passi.map((passo, indice) => (
            <li key={passo} className="flex items-center gap-3">
              <span className="flex size-7 shrink-0 items-center justify-center rounded-full bg-oro leading-[1.4] font-semibold">
                {indice + 1}
              </span>
              {passo}
            </li>
          ))}
        </ol>

        {/* Sembra un bottone, ma è parte del link: tutta la card è cliccabile */}
        <span className="mt-auto flex h-12 items-center justify-center rounded-full bg-oro-bordo px-5 font-medium group-hover:brightness-95">
          {testoBottone}
        </span>
      </div>
    </Link>
  )
}
