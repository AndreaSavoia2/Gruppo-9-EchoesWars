import { Link } from 'react-router'
import AppLayout from '../components/AppLayout'
import Intestazione from '../components/Intestazione'
import Opzione from '../components/Opzione'
import CardModalita from '../components/CardModalita'

// Le 2 modalità di gioco. Testi fissi presi dal Figma (non arrivano dal backend).
// Su mobile si usa la descrizione corta, su desktop quella lunga + i passi.
const modalita = [
  {
    id: 'casuale',
    percorso: '/gioca/casuale',
    titolo: 'Partita casuale',
    descrizioneCorta: 'Ti abbiniamo a un altro Risonante',
    descrizione: 'Ti abbiniamo a un altro Risonante e la partita parte subito.',
    iconaMobile: '/icons/partita-casuale.svg',
    iconaDesktop: '/icons/spade.svg',
    immagine: '/images/modalita-casuale.png',
    passi: [
      'Scegli uno dei tuoi mazzi validi',
      'Il sistema trova un avversario',
      'Guarda il risultato e, se vuoi, il replay',
    ],
    testoBottone: 'Gioca una partita casuale',
  },
  {
    id: 'amico',
    percorso: '/gioca/amico',
    titolo: 'Sfida un amico',
    descrizioneCorta: 'Scegli un avversario dalla tua lista amici',
    descrizione: 'Scegli chi sfidare tra i tuoi amici e mandagli una richiesta.',
    iconaMobile: '/icons/aggiungi-amico.svg',
    iconaDesktop: '/icons/aggiungi-amico.svg',
    immagine: '/images/sfondo-desktop.png',
    passi: [
      'Scegli l’amico da sfidare',
      'Scegli il mazzo e invia la sfida',
      'Quando accetta, vedi il risultato',
    ],
    testoBottone: 'Scegli un amico',
  },
]

export default function ComeVuoiGiocare() {
  return (
    <AppLayout paginaAttiva="gioca" sfondoMobile="/images/sfondo-gioca.png">
      <div className="flex flex-col gap-6">
        <Intestazione titolo="Come vuoi giocare?" sottotitolo="Prima scegli l’avversario, poi il mazzo.">
          {/* Link "‹ Home": solo su desktop (su mobile c'è il menu in basso) */}
          <Link to="/" className="hidden font-medium underline lg:block">
            ‹ Home
          </Link>
        </Intestazione>

        {/* 📱 Mobile: righe compatte */}
        <div className="flex flex-col gap-2.5 lg:hidden">
          {modalita.map((m) => (
            <Opzione
              key={m.id}
              percorso={m.percorso}
              icona={m.iconaMobile}
              titolo={m.titolo}
              descrizione={m.descrizioneCorta}
            />
          ))}
        </div>

        {/* 💻 Desktop: due card grandi affiancate */}
        <div className="hidden grid-cols-2 gap-6 lg:grid">
          {modalita.map((m) => (
            <CardModalita
              key={m.id}
              percorso={m.percorso}
              immagine={m.immagine}
              icona={m.iconaDesktop}
              titolo={m.titolo}
              descrizione={m.descrizione}
              passi={m.passi}
              testoBottone={m.testoBottone}
            />
          ))}
        </div>
      </div>
    </AppLayout>
  )
}
