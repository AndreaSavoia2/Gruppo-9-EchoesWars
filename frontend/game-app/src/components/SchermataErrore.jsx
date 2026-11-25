import AppLayout from './AppLayout'

// Colori del cerchio con l'icona, uno per tipo di problema (dal Figma E1-E3)
const coloriIcona = {
  info: 'border-info bg-info-sfondo', //       E1 · Sei offline (viola)
  errore: 'border-errore bg-errore-sfondo', //  E2 · Server non raggiungibile (rosso)
  avviso: 'border-oro bg-sfondo', //            E3 · Partita interrotta (oro)
}

// Schermata intera per gli errori "grossi": titolo, spiegazione, icona e bottoni in fondo.
// I bottoni li passa chi la usa (children), perché cambiano da errore a errore.
//
// Esempio:
//   <SchermataErrore titolo="Sei offline" testo="Controlla..." icona="📶" colore="info">
//     <Bottone onClick={riprova}>Riprova</Bottone>
//   </SchermataErrore>
export default function SchermataErrore({ titolo, testo, icona, colore = 'errore', children }) {
  return (
    <AppLayout sfondoMobile="/images/sfondo-gioca.png" nascondiMenuMobile>
      {/* role="alert": lo screen reader legge subito il messaggio */}
      <div role="alert" className="mx-auto flex w-full max-w-xl flex-1 flex-col items-center gap-6 text-center lg:justify-center">
        <div className="flex flex-col gap-3">
          <h1 className="font-titolo text-[22px] lg:text-h2">{titolo}</h1>
          <p>{testo}</p>
        </div>

        {/* aria-hidden: l'emoji è solo decorativa, il titolo dice già tutto */}
        <div
          aria-hidden="true"
          className={`mt-8 flex size-28 items-center justify-center rounded-full border-2 text-h2 ${coloriIcona[colore]}`}
        >
          {icona}
        </div>

        {/* mt-auto: su mobile spinge i bottoni in fondo allo schermo */}
        <div className="mt-auto flex w-full flex-col gap-3 lg:mt-8 lg:flex-row lg:justify-center [&>*]:lg:w-auto [&>*]:lg:min-w-50">
          {children}
        </div>
      </div>
    </AppLayout>
  )
}
