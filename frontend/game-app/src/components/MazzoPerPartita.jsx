import { Link } from 'react-router'

// Un mazzo (dal Figma "App/Mazzo per partita").
// 3 stati: Valido · Selezionato (bordo spesso + "✓ Scelto") · Non valido (sbiadito + 🔒)
// - con onScegli → è un bottone (schermata "Scegli il mazzo")
// - con percorso → è un link (lista mazzi: si apre per modificarlo, anche se non valido)
export default function MazzoPerPartita({ mazzo, selezionato = false, onScegli, percorso }) {
  const { nome, valido, motivo, punti, puntiMax } = mazzo
  // Sotto il nome: i punti se è valido, altrimenti il motivo
  const info = valido ? `${punti}/${puntiMax} punti` : motivo

  let stile = 'border border-oro bg-sfondo' // valido
  if (selezionato) stile = 'border-3 border-testo bg-sfondo-secondario'
  if (!valido) stile = 'border border-oro bg-sfondo-secondario'

  const contenuto = (
    <>
      {/* Pila di 3 carte: 2 retri dietro + il fronte davanti */}
      <span className={`relative block h-29 w-22 shrink-0 ${valido ? '' : 'opacity-40'}`} aria-hidden="true">
        <img src="/images/mazzo/retro-2.png" alt="" className="absolute top-0 left-3.5 h-27 w-18 rounded-md object-cover opacity-55" />
        <img src="/images/mazzo/retro-1.png" alt="" className="absolute top-1 left-1.5 h-27 w-18 rounded-md object-cover opacity-80" />
        <img src="/images/mazzo/fronte.png" alt="" className="absolute top-1.5 left-0 h-27 w-18 rounded-md object-cover" />
      </span>

      <span
        className={`flex min-h-24 w-full flex-1 flex-col items-center justify-center gap-0.5 rounded-[10px] px-1 py-1.5 text-center leading-[1.4] ${stile}`}
      >
        <span className="line-clamp-2 font-semibold">{nome}</span>
        {selezionato ? (
          <span className="text-didascalia font-medium">✓ Scelto</span>
        ) : valido ? (
          <span className="text-didascalia font-medium text-successo">✓ Valido</span>
        ) : (
          <span className="text-didascalia font-medium text-errore">🔒 Non valido</span>
        )}
        <span className="w-full truncate text-didascalia font-medium">{info}</span>
      </span>
    </>
  )

  const classi = 'flex min-h-58 flex-col items-center gap-1.5 lg:w-35'

  if (percorso) {
    return (
      <Link to={percorso} className={classi}>
        {contenuto}
      </Link>
    )
  }

  return (
    <button
      type="button"
      onClick={onScegli}
      disabled={!valido}
      aria-pressed={selezionato}
      className={`${classi} disabled:cursor-not-allowed`}
    >
      {contenuto}
    </button>
  )
}
