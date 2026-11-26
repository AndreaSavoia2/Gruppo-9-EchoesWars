import { Link } from 'react-router'
import AppLayout from '../components/AppLayout'
import PageHeader from '../components/PageHeader'
import OptionButton from '../components/OptionButton'
import ModeCard from '../components/ModeCard'

// The 2 game modes. Fixed texts taken from the Figma (they do not come from the backend).
// On mobile the short description is used, on desktop the long one + the steps.
const mode = [
  {
    id: 'random',
    path: '/play/random',
    title: 'Partita casuale',
    shortDescription: 'Ti abbiniamo a un altro Risonante',
    description: 'Ti abbiniamo a un altro Risonante e la partita parte subito.',
    mobileIcon: '/icons/random-match.svg',
    desktopIcon: '/icons/swords.svg',
    image: '/images/random-mode.png',
    steps: [
      'Scegli uno dei tuoi mazzi validi',
      'Il sistema trova un avversario',
      'Guarda il risultato e, se vuoi, il replay',
    ],
    buttonText: 'Gioca una partita casuale',
  },
  {
    id: 'friend',
    path: '/play/friend',
    title: 'Sfida un amico',
    shortDescription: 'Scegli un avversario dalla tua lista amici',
    description: 'Scegli chi sfidare tra i tuoi amici e mandagli una richiesta.',
    mobileIcon: '/icons/add-friend.svg',
    desktopIcon: '/icons/add-friend.svg',
    image: '/images/desktop-background.png',
    steps: [
      'Scegli l’amico da sfidare',
      'Scegli il mazzo e invia la sfida',
      'Quando accetta, vedi il risultato',
    ],
    buttonText: 'Scegli un amico',
  },
]

/**
 * "Come vuoi giocare?" (how do you want to play?) page: random match or challenge a friend.
 *
 * @returns {JSX.Element}
 */
export default function HowToPlay() {
  return (
    <AppLayout activePage="play" mobileBackground="/images/play-background.png">
      <div className="flex flex-col gap-6">
        <PageHeader title="Come vuoi giocare?" subtitle="Prima scegli l’avversario, poi il mazzo.">
          {/* "‹ Home" link: desktop only (on mobile there is the bottom menu) */}
          <Link to="/" className="hidden font-medium underline lg:block">
            ‹ Home
          </Link>
        </PageHeader>

        {/* 📱 Mobile: compact rows */}
        <div className="flex flex-col gap-2.5 lg:hidden">
          {mode.map((m) => (
            <OptionButton
              key={m.id}
              path={m.path}
              icon={m.mobileIcon}
              title={m.title}
              description={m.shortDescription}
            />
          ))}
        </div>

        {/* 💻 Desktop: two big cards side by side */}
        <div className="hidden grid-cols-2 gap-6 lg:grid">
          {mode.map((m) => (
            <ModeCard
              key={m.id}
              path={m.path}
              image={m.image}
              icon={m.desktopIcon}
              title={m.title}
              description={m.description}
              steps={m.steps}
              buttonText={m.buttonText}
            />
          ))}
        </div>
      </div>
    </AppLayout>
  )
}
