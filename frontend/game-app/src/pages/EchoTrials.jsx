import { useState } from 'react'
import { Link } from 'react-router'
import AppLayout from '../components/AppLayout'
import PageHeader from '../components/PageHeader'
import Notice from '../components/Notice'
import MissionRow from '../components/MissionRow'
import Skeleton from '../components/Skeleton'
import ErrorMessage from '../components/ErrorMessage'
import { useFetch } from '../hooks/useFetch'
import { useUser } from '../context/UserContext'
import { getTrials, claimTrial } from '../api/progress'

const VISIBLE = 4 // how many trials to show before "Mostra tutte" (show all)

/**
 * 13 · 13e / D10 · /missions
 *
 * @returns {JSX.Element}
 */
export default function EchoTrials() {
  const { data, loading, error, reload } = useFetch(getTrials)
  const { reload: reloadUser } = useUser()
  const [all, setAll] = useState(false)
  const [outcome, setOutcome] = useState(null)
  const [inProgress, setInProgress] = useState(null)

  /** Claims the reward of a completed trial. */
  async function claim(trial) {
    setInProgress(trial.id)
    try {
      const { creditsAdded } = await claimTrial(trial.id)
      setOutcome({ kind: 'success', text: `+${creditsAdded} crediti aggiunti · ${data.completedCount + 1} di ${data.totals} Prove completate.` })
      reload()
      reloadUser()
    } catch {
      setOutcome({ kind: 'error', text: 'Non è stato possibile riscuotere. Riprova.' })
    }
    setInProgress(null)
  }

  const trials = data ? (all ? data.trials : data.trials.slice(0, VISIBLE)) : []

  return (
    <AppLayout activePage="play">
      <div className="flex flex-col gap-2.5 lg:gap-6">
        <PageHeader title="Prove degli Echi" subtitle="12 obiettivi uguali per tutti, completabili una sola volta per account.">
          <Link to="/" className="hidden font-medium underline lg:block">
            ‹ Gioca
          </Link>
        </PageHeader>

        {loading && <Skeleton className="mt-3 h-80 rounded-2xl" />}
        {error && <ErrorMessage />}

        {data && (
          <section className="flex flex-col gap-2.5 pt-3 lg:max-w-[610px] lg:rounded-2xl lg:border lg:border-gold lg:bg-background lg:p-4 lg:pt-6">
            <h2 className="hidden font-title text-h2 lg:block">
              Prove degli Echi · {data.completedCount}/{data.totals}
            </h2>
            {outcome ? (
              <Notice kind={outcome.kind}>{outcome.text}</Notice>
            ) : (
              <Notice kind="info" icon="">
                {data.completedCount} di {data.totals} Prove completate · {data.creditsGained} di {data.totalCredits} crediti ottenuti.
              </Notice>
            )}
            <ul className="flex flex-col gap-2.5">
              {trials.map((trial) => (
                <li key={trial.id}>
                  <MissionRow {...trial} onClaim={() => claim(trial)} inProgress={inProgress === trial.id} />
                </li>
              ))}
            </ul>
            {!all && data.trials.length > VISIBLE && (
              <button type="button" onClick={() => setAll(true)} className="self-start text-caption font-medium underline">
                Mostra tutte le {data.trials.length} Prove
              </button>
            )}
          </section>
        )}
      </div>
    </AppLayout>
  )
}
