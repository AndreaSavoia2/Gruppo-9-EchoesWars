import { useState } from 'react'
import { useNavigate } from 'react-router'
import Bottone from '../components/Bottone'
import { useUtente } from '../context/UtenteContext'
import { segnaBenvenutoVisto as inviaBenvenutoVisto } from '../api/utente'
import { REGOLE_MAZZO } from '../data/regole'

// I 3 passi del benvenuto (Figma O1-O3).
// "adatta": cover = l'immagine riempie tutto (tagliando i bordi),
//           contain = l'immagine si vede intera (la busta).
const PASSI = [
  {
    immagine: '/images/sfondo-desktop.png',
    adatta: 'object-cover',
    titolo: 'Benvenuto, Risonante',
    testo: 'Echoes’ War è un gioco di carte collezionabili digitale. Collezioni combattenti, prepari i mazzi e sfidi altri giocatori.',
  },
  {
    immagine: '/images/sfondo-mazzo.png',
    adatta: 'object-cover',
    titolo: 'Prepara il tuo mazzo',
    testo: `Scegli ${REGOLE_MAZZO.echi} Echi e ${REGOLE_MAZZO.carte} carte, con un costo massimo di ${REGOLE_MAZZO.punti} punti. In battaglia non devi scegliere le mosse: la sfida è automatica.`,
  },
  {
    immagine: '/images/busta.png',
    adatta: 'object-contain p-6',
    titolo: 'Gioca e guadagna crediti',
    testo: 'Ogni partita ti dà crediti: 10 per una vittoria, 5 per un pareggio, 2 per una sconfitta. Con 100 crediti apri una busta da 5 carte.',
  },
]

// /benvenuto → schermate del primo accesso. Una sola pagina che cambia "passo".
// Ci arrivi da solo al primo accesso (vedi ControlloApp: utente.primoAccesso = true).
export default function PrimoAccesso() {
  const [passo, setPasso] = useState(0) // 0, 1 o 2
  const navigate = useNavigate()
  const { segnaBenvenutoVisto, ricarica } = useUtente()
  const ultimo = passo === PASSI.length - 1
  const { immagine, adatta, titolo, testo } = PASSI[passo]

  // "Salta" o "Inizia a giocare": avvisa il backend e vai alla home.
  // Se l'avviso fallisce non blocchiamo l'utente: al massimo rivedrà il benvenuto la prossima volta.
  const concludi = () => {
    segnaBenvenutoVisto()
    inviaBenvenutoVisto()
      .catch(() => {})
      .finally(ricarica)
    navigate('/', { replace: true })
  }

  return (
    // Mobile: immagine sopra + pannello sotto. Desktop: immagine a sinistra + pannello a destra.
    <main className="flex min-h-screen flex-col bg-sfondo lg:flex-row">
      {/* key={passo}: cambiando passo l'immagine "rinasce" e rifà l'animazione di comparsa */}
      <div className="relative h-[58vh] shrink-0 lg:h-auto lg:flex-1">
        <img key={passo} src={immagine} alt="" className={`absolute inset-0 size-full animate-[comparsa_0.3s_ease-out] ${adatta}`} />
      </div>

      {/* -mt-6 + angoli arrotondati: il pannello "sale" un po' sopra l'immagine (solo mobile) */}
      <section
        aria-labelledby="titolo-benvenuto"
        className="relative -mt-6 flex flex-1 flex-col gap-3.5 rounded-t-3xl bg-sfondo px-5 pt-7 pb-8 lg:mt-0 lg:max-w-xl lg:justify-center lg:rounded-none lg:px-16"
      >
        {/* Pallini di avanzamento: quello attivo è più largo e scuro */}
        <div className="flex items-center gap-2">
          {PASSI.map((_, i) => (
            <span
              key={i}
              aria-hidden="true"
              className={`h-2.5 rounded-full transition-all ${i === passo ? 'w-7 bg-testo' : 'w-2.5 bg-oro'}`}
            />
          ))}
          <p className="ml-1 text-didascalia">
            Passo {passo + 1} di {PASSI.length}
          </p>
        </div>

        <h1 id="titolo-benvenuto" className="font-titolo text-[22px] lg:text-h2">
          {titolo}
        </h1>
        <p className="lg:min-h-18">{testo}</p>

        <div className="mt-auto flex flex-col gap-3 pt-4 lg:mt-4">
          {ultimo ? (
            <Bottone onClick={concludi}>Inizia a giocare</Bottone>
          ) : (
            <>
              <Bottone onClick={() => setPasso(passo + 1)}>Avanti</Bottone>
              <button onClick={concludi} className="min-h-11 font-medium underline underline-offset-4">
                Salta
              </button>
            </>
          )}
        </div>
      </section>
    </main>
  )
}
