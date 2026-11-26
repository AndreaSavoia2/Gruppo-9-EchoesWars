import { useState } from 'react'
import { useNavigate } from 'react-router'
import Button from '../components/Button'
import { useUser } from '../context/UserContext'
import { markWelcomeSeen as sendWelcomeSeen } from '../api/user'
import { DECK_RULES } from '../data/rules'

// The 3 welcome steps (Figma O1-O3).
// "fit": cover = the image fills everything (cutting the edges),
//        contain = the whole image is visible (the pack).
const STEPS = [
  {
    image: '/images/desktop-background.png',
    fit: 'object-cover',
    title: 'Benvenuto, Risonante',
    text: 'Echoes’ War è un gioco di carte collezionabili digitale. Collezioni combattenti, prepari i mazzi e sfidi altri giocatori.',
  },
  {
    image: '/images/deck-background.png',
    fit: 'object-cover',
    title: 'Prepara il tuo mazzo',
    text: `Scegli ${DECK_RULES.echoes} Echi e ${DECK_RULES.cards} carte, con un costo massimo di ${DECK_RULES.points} punti. In battaglia non devi scegliere le mosse: la sfida è automatica.`,
  },
  {
    image: '/images/pack.png',
    fit: 'object-contain p-6',
    title: 'Gioca e guadagna crediti',
    text: 'Ogni partita ti dà crediti: 10 per una vittoria, 5 per un pareggio, 2 per una sconfitta. Con 100 crediti apri una busta da 5 carte.',
  },
]

/**
 * /welcome → first login screens. A single page that changes "step".
 * You get here by yourself at the first login (see AppGuard: user.firstLogin = true).
 *
 * @returns {JSX.Element}
 */
export default function FirstLogin() {
  const [step, setStep] = useState(0) // 0, 1 or 2
  const navigate = useNavigate()
  const { markWelcomeSeen, reload } = useUser()
  const last = step === STEPS.length - 1
  const { image, fit, title, text } = STEPS[step]

  // "Salta" (skip) or "Inizia a giocare" (start playing): tell the backend and go to the home.
  // If the notice fails we do not block the user: at worst they will see the welcome again next time.
  const finish = () => {
    markWelcomeSeen()
    sendWelcomeSeen()
      .catch(() => {})
      .finally(reload)
    navigate('/', { replace: true })
  }

  return (
    // Mobile: image above + panel below. Desktop: image on the left + panel on the right.
    <main className="flex min-h-screen flex-col bg-background lg:flex-row">
      {/* key={step}: when the step changes the image is "reborn" and plays the appear animation again */}
      <div className="relative h-[58vh] shrink-0 lg:h-auto lg:flex-1">
        <img key={step} src={image} alt="" className={`absolute inset-0 size-full animate-[comparsa_0.3s_ease-out] ${fit}`} />
      </div>

      {/* -mt-6 + rounded corners: the panel "rises" a bit over the image (mobile only) */}
      <section
        aria-labelledby="welcome-title"
        className="relative -mt-6 flex flex-1 flex-col gap-3.5 rounded-t-3xl bg-background px-5 pt-7 pb-8 lg:mt-0 lg:max-w-xl lg:justify-center lg:rounded-none lg:px-16"
      >
        {/* Progress dots: the active one is wider and darker */}
        <div className="flex items-center gap-2">
          {STEPS.map((_, i) => (
            <span
              key={i}
              aria-hidden="true"
              className={`h-2.5 rounded-full transition-all ${i === step ? 'w-7 bg-ink' : 'w-2.5 bg-gold'}`}
            />
          ))}
          <p className="ml-1 text-caption">
            Passo {step + 1} di {STEPS.length}
          </p>
        </div>

        <h1 id="welcome-title" className="font-title text-[22px] lg:text-h2">
          {title}
        </h1>
        <p className="lg:min-h-18">{text}</p>

        <div className="mt-auto flex flex-col gap-3 pt-4 lg:mt-4">
          {last ? (
            <Button onClick={finish}>Inizia a giocare</Button>
          ) : (
            <>
              <Button onClick={() => setStep(step + 1)}>Avanti</Button>
              <button onClick={finish} className="min-h-11 font-medium underline underline-offset-4">
                Salta
              </button>
            </>
          )}
        </div>
      </section>
    </main>
  )
}
