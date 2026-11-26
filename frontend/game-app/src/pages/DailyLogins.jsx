import { Link } from 'react-router'
import AppLayout from '../components/AppLayout'
import PageHeader from '../components/PageHeader'
import Skeleton from '../components/Skeleton'
import ErrorMessage from '../components/ErrorMessage'
import { useFetch } from '../hooks/useFetch'
import { getLogins } from '../api/progress'

/**
 * 14 / D14 · /logins — calendar of the consecutive logins
 *
 * @returns {JSX.Element}
 */
export default function DailyLogins() {
  const { data, loading, error } = useFetch(getLogins)

  const rewardOf = (day) => data?.rewards.find((r) => r.day === day)
  const nextOne = data?.rewards.find((r) => r.day > data.day)

  return (
    <AppLayout mobileBackground={null}>
      <div className="flex flex-col gap-2.5 lg:gap-6">
        <PageHeader title="Accessi consecutivi" subtitle={data ? `Giorno ${data.day} di ${data.total}` : ' '}>
          <p className="hidden lg:block">
            Ogni 5 giorni consecutivi di accesso sblocchi una ricompensa. Se salti un giorno, il conteggio riparte dal giorno 1; dopo
            il giorno 30 il ciclo ricomincia.
          </p>
          <Link to="/profile" className="hidden font-medium underline lg:block">
            ‹ Profilo
          </Link>
        </PageHeader>
        <Link to="/profile" className="mt-3 self-start font-medium underline lg:hidden">
          ‹ Profilo
        </Link>
        <p className="text-caption leading-[1.4] font-medium lg:hidden">
          Ogni 5 giorni consecutivi sblocchi una ricompensa. Se salti un giorno, riparti dal giorno 1.
        </p>

        {loading && <Skeleton className="h-80 rounded-2xl" />}
        {error && <ErrorMessage />}

        {data && (
          <div className="flex flex-col gap-2.5 lg:flex-row lg:items-start lg:gap-6">
            <section className="flex flex-col gap-2.5 lg:flex-1 lg:rounded-2xl lg:border lg:border-gold lg:bg-background lg:p-6">
              <h2 className="hidden font-title text-h2 lg:block">
                Giorno {data.day} di {data.total}
              </h2>
              <ol className="grid grid-cols-6 gap-2.5 lg:grid-cols-8">
                {Array.from({ length: data.total }, (_, i) => i + 1).map((day) => (
                  <li key={day}>
                    <Day count={day} today={data.day} gift={Boolean(rewardOf(day))} />
                  </li>
                ))}
              </ol>
              <p className="text-caption leading-[1.4] font-medium">✓ Accesso fatto &nbsp; ▢ Oggi (bordo spesso) &nbsp; 🎁 Giorno con ricompensa</p>
            </section>

            <div className="flex flex-col gap-4 lg:w-100 lg:shrink-0">
              {nextOne && (
                <section className="flex flex-col gap-1 rounded-2xl border-2 border-gold-border bg-background px-4 py-3.5 leading-[1.4]">
                  <h2 className="font-semibold lg:text-caption">
                    <span className="lg:hidden">Prossima: 🎁 {nextOne.name}</span>
                    <span className="hidden lg:inline">Prossima ricompensa</span>
                  </h2>
                  <p className="hidden font-title text-h2 leading-normal lg:block">🎁 {nextOne.name}</p>
                  <p className="text-caption font-medium">
                    Al giorno {nextOne.day} · mancano {nextOne.day - data.day} accessi
                  </p>
                </section>
              )}

              {/* 💻 Desktop: all the rewards of the cycle */}
              <section className="hidden flex-col gap-2 rounded-2xl border border-gold bg-background p-4 lg:flex">
                <h2 className="font-title text-h2">Ricompense del ciclo</h2>
                <ul className="flex flex-col gap-2">
                  {data.rewards.map((r) => (
                    <li
                      key={r.day}
                      className={`flex items-center justify-between rounded-lg px-2.5 py-2 ${r.unlocked ? 'bg-success-background' : 'bg-background-secondary'}`}
                    >
                      <span className="font-semibold">Giorno {r.day}</span>
                      <span className={`text-caption ${r.unlocked ? 'text-success' : ''}`}>
                        {r.unlocked ? `✓ ${r.name} · sbloccata` : r.name}
                      </span>
                    </li>
                  ))}
                </ul>
              </section>
            </div>
          </div>
        )}
      </div>
    </AppLayout>
  )
}

/**
 * A calendar day (from the Figma "App/Giorno accesso"): Done · Today · Future (🎁 if there is a reward)
 *
 * @param {object} props - the component props
 * @param {number} props.count - day number
 * @param {number} props.today - today's day number
 * @param {object} [props.gift] - the reward of that day, if any
 * @returns {JSX.Element}
 */
function Day({ count, today, gift }) {
  const isDone = count < today
  const isToday = count === today
  let styleName = 'border border-gold bg-background-secondary'
  if (isDone) styleName = 'border border-success bg-success-background'
  if (isToday) styleName = 'border-3 border-ink bg-background'

  return (
    <span
      aria-current={isToday ? 'date' : undefined}
      className={`flex aspect-square w-full flex-col items-center justify-center rounded-xl leading-[1.4] font-semibold lg:size-11.5 ${styleName}`}
    >
      {isDone && <span className="text-success" aria-hidden="true">✓</span>}
      {!isDone && !isToday && gift && <span aria-hidden="true">🎁</span>}
      {count}
      {isToday && <span className="text-caption font-medium">Oggi</span>}
      {isDone && <span className="sr-only">, fatto</span>}
      {gift && !isDone && <span className="sr-only">, ricompensa</span>}
    </span>
  )
}
