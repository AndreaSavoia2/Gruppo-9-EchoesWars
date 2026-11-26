import { useCallback, useState } from 'react'
import AppLayout from '../components/AppLayout'
import PageHeader from '../components/PageHeader'
import Button, { LinkButton } from '../components/Button'
import Modal from '../components/Modal'
import LinkRow from '../components/LinkRow'
import Skeleton from '../components/Skeleton'
import ErrorMessage from '../components/ErrorMessage'
import { useFetch } from '../hooks/useFetch'
import { exit, getProfile } from '../api/profile'
import { goToLogin } from '../api/token'

/**
 * 11 / D11 · /profile
 *
 * @returns {JSX.Element}
 */
export default function Profile() {
  const { data: profile, loading, error } = useFetch(getProfile)
  const [confirmExit, setConfirmExit] = useState(false)
  const close = useCallback(() => setConfirmExit(false), [])

  /** Logs out and goes back to the login page. */
  async function logOut() {
    await exit() // tells the backend and deletes the token
    // Back to the static login (or, if it is not configured, to the app Home)
    if (!goToLogin('uscito')) window.location.href = '/'
  }

  const logins = profile?.logins
  // Every entry: on which screens it appears (from the Figma, mobile and desktop have slightly different entries)
  const entries = profile
    ? [
        { title: 'Modifica avatar', detail: 'L’immagine accanto al tuo nome', icon: '/icons/pencil.svg', path: '/profile/avatar' },
        { title: 'Modifica dati', detail: 'Nome utente, email e password', icon: '/icons/pencil.svg', path: '/profile/data' },
        { title: 'Cronologia partite', detail: `${profile.matches} partite giocate`, icon: '/icons/clock.svg', path: '/history', only: 'desktop' },
        { title: 'Lingua', detail: profile.language, icon: '/icons/globe.svg', path: '/profile/language' },
        {
          title: 'Accessi consecutivi',
          // ⚠️ Consecutive logins are not in the backend yet (logins = null)
          detail: logins
            ? `Giorno ${logins.day} di ${logins.total} · prossima ricompensa al ${logins.nextReward.day}`
            : 'Presto disponibile',
          icon: '/icons/clock.svg',
          path: '/logins',
          only: 'mobile',
        },
        { title: 'Cambia password', detail: 'Serve la password attuale', icon: '/icons/pencil.svg', path: '/profile/password', only: 'desktop' },
      ]
    : []
  const visibility = { mobile: 'lg:hidden', desktop: 'hidden lg:block' }

  return (
    <AppLayout mobileBackground={null}>
      <div className="flex flex-col gap-2.5 lg:gap-6">
        <PageHeader title="Profilo" subtitle={profile ? `${profile.name} · ${profile.email}` : ' '} />

        {loading && <Skeleton className="mt-3 h-80 rounded-2xl" />}
        {error && <ErrorMessage />}

        {profile && (
          <div className="flex flex-col gap-2.5 pt-3 lg:flex-row lg:items-start lg:gap-6 lg:pt-0">
            <div className="flex flex-col gap-4 lg:flex-1">
              <dl className="flex gap-2.5 lg:gap-1">
                {[
                  ['Partite', profile.matches],
                  ['Vittorie', profile.wins],
                  ['Carte', profile.cardCount],
                ].map(([name, value]) => (
                  <div key={name} className="flex flex-1 flex-col-reverse items-center gap-0.5 rounded-[14px] bg-background-secondary py-3">
                    <dt className="text-caption leading-[1.4] font-medium text-ink-secondary">{name}</dt>
                    <dd className="font-title text-[22px] lg:text-h2">{value}</dd>
                  </div>
                ))}
              </dl>

              {/* 💻 Desktop: Consecutive logins box */}
              <section className="hidden flex-col items-start gap-2.5 rounded-2xl border border-gold bg-background p-6 lg:flex">
                <h2 className="font-title text-h2">Accessi consecutivi</h2>
                {logins ? (
                  <p>
                    Sei al giorno {logins.day} di {logins.total}. Prossima ricompensa al giorno{' '}
                    {logins.nextReward.day}: {logins.nextReward.name}.
                  </p>
                ) : (
                  <p>Presto disponibile.</p>
                )}
                <LinkButton to="/logins" variant="secondary" className="min-h-12 w-auto">
                  Vedi le ricompense
                </LinkButton>
              </section>
            </div>

            <section className="flex flex-col gap-2.5 lg:w-100 lg:shrink-0 lg:gap-3">
              <h2 className="hidden font-title text-h2 lg:block">Impostazioni</h2>
              <ul className="flex flex-col gap-2.5 lg:gap-3">
                {entries.map((entry) => (
                  <li key={entry.title} className={visibility[entry.only] ?? ''}>
                    <LinkRow {...entry} />
                  </li>
                ))}
              </ul>
              <Button variant="destructive" onClick={() => setConfirmExit(true)} className="w-full lg:min-h-12">
                Esci dall’account
              </Button>
            </section>
          </div>
        )}
      </div>

      {confirmExit && (
        <Modal title="Uscire dall’account?" onClose={close}>
          <p className="text-ink-secondary">Per giocare di nuovo dovrai accedere con email e password.</p>
          <Button variant="destructive" onClick={logOut}>
            Esci
          </Button>
          <Button variant="secondary" onClick={close}>
            Annulla
          </Button>
        </Modal>
      )}
    </AppLayout>
  )
}
