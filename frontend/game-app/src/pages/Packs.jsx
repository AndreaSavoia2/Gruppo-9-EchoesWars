import { useState } from 'react'
import { useNavigate } from 'react-router'
import AppLayout from '../components/AppLayout'
import PageHeader from '../components/PageHeader'
import Button from '../components/Button'
import Skeleton from '../components/Skeleton'
import ErrorMessage from '../components/ErrorMessage'
import LegendaryProgress from '../components/LegendaryProgress'
import CollectionTabs from '../components/collection/CollectionTabs'
import PackOpening from '../components/packs/PackOpening'
import { useFetch } from '../hooks/useFetch'
import { openPacks, getPacks } from '../api/packs'

/**
 * 8 · /collection/packs — the packs to open
 *
 * @returns {JSX.Element}
 */
export default function Packs() {
  const navigate = useNavigate()
  const { data, loading, error, reload } = useFetch(getPacks)
  const [opening, setOpening] = useState(null) // the result to show in the animation
  const [sending, setSending] = useState({ inProgress: false, error: null })

  /** Opens the packs and shows the animation. */
  async function openPack(amount) {
    setSending({ inProgress: true, error: null })
    try {
      const result = await openPacks(amount)
      setOpening(result)
      setSending({ inProgress: false, error: null })
      reload() // refreshes the number of packs and the legendary
    } catch {
      setSending({ inProgress: false, error: 'Non è stato possibile aprire la busta. Riprova.' })
    }
  }

  const availableItems = data?.availableItems ?? 0

  return (
    <AppLayout activePage="collection" mobileBackground="/images/packs-background.png" desktopBackground="/images/collection-background.png">
      <div className="flex flex-col items-center gap-3.5 lg:items-start lg:gap-6">
        <div className="w-full">
          <PageHeader
            title="Collezione"
            subtitle={data ? (availableItems === 0 ? 'Non hai buste da aprire.' : `Hai ${availableItems} buste da aprire.`) : ' '}
          />
        </div>

        <div className="w-full pt-3 lg:pt-0">
          <CollectionTabs active="packs" />
        </div>

        {loading && <Skeleton className="h-67 w-50 rounded-xl" />}
        {error && <ErrorMessage />}

        {data && (
          <div className="flex w-full flex-col items-center gap-3.5 lg:max-w-md">
            {/* Pack in the foreground with the number */}
            <div className="relative">
              <img
                src="/images/pack.png"
                alt="Busta Echoes’ War"
                className={`h-67 w-50 rounded-xl object-cover shadow-[0_8px_28px_rgba(184,138,66,0.45)] ${availableItems === 0 ? 'opacity-40' : ''}`}
              />
              {availableItems > 0 && (
                <span className="absolute -top-2 -right-1 rounded-full bg-info px-2.5 py-1 leading-[1.4] font-semibold text-background">
                  ×{availableItems}
                  <span className="sr-only"> buste</span>
                </span>
              )}
            </div>

            <div className="w-full">
              <LegendaryProgress packs={data.legendary.packs} total={data.legendary.total} />
            </div>

            <Button onClick={() => openPack(1)} disabled={availableItems === 0 || sending.inProgress}>
              {sending.inProgress ? 'Apertura…' : 'Apri busta'}
            </Button>
            {availableItems > 1 && (
              <Button variant="secondary" onClick={() => openPack(availableItems)} disabled={sending.inProgress}>
                Apri tutte ({availableItems})
              </Button>
            )}
            {sending.error && <ErrorMessage text={sending.error} />}
          </div>
        )}
      </div>

      {opening && (
        <PackOpening
          cardList={opening.cardList}
          summary={opening.summary}
          onDone={() => navigate('/collection')}
        />
      )}
    </AppLayout>
  )
}
