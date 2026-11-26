import { useCallback, useEffect, useState } from 'react'
import AppLayout from '../components/AppLayout'
import PageHeader from '../components/PageHeader'
import { LinkButton } from '../components/Button'
import Skeleton from '../components/Skeleton'
import ErrorMessage from '../components/ErrorMessage'
import MatchDeck from '../components/MatchDeck'
import CollectionTabs from '../components/collection/CollectionTabs'
import Filter from '../components/collection/Filter'
import { useFetch } from '../hooks/useFetch'
import { getDeckFilters, getDecks } from '../api/decks'

const FILTERS = [
  { key: 'school', label: 'Scuola' },
  { key: 'echo', label: 'Eco' },
  { key: 'sort', label: 'Ordina', title: 'Ordina per', single: true },
]
const NO_FILTERS = { school: [], echo: [], sort: [] }

/**
 * 5c · 5k · 5l · 5m · 5p / D4 · /collection/decks — your decks
 *
 * @returns {JSX.Element}
 */
export default function Decks() {
  const [filters, setFilters] = useState(NO_FILTERS)
  const [openFilter, setOpenFilter] = useState(null)

  const loadDecks = useCallback((signal) => getDecks(signal, filters), [filters])
  const { data, loading, error } = useFetch(loadDecks)
  const { data: options } = useFetch(getDeckFilters)

  // Esc key = close the menu of the open filter
  useEffect(() => {
    if (!openFilter) return
    const onKey = (event) => event.key === 'Escape' && setOpenFilter(null)
    window.addEventListener('keydown', onKey)
    return () => window.removeEventListener('keydown', onKey)
  }, [openFilter])

  const isFull = data && data.total >= data.maximum
  const noDeck = data?.total === 0

  return (
    <AppLayout activePage="collection" mobileBackground="/images/deck-background.png" desktopBackground="/images/deck-background-desktop.png">
      <div className="flex flex-col gap-3 lg:gap-6">
        <PageHeader title="Collezione" subtitle="Modifica o crea i tuoi mazzi." />

        <div className="flex flex-col gap-3 pt-3 lg:flex-row lg:items-center lg:gap-6 lg:pt-0">
          <CollectionTabs active="decks" />

          {/* 📱 Mobile: the counter sits between the tabs and the filters */}
          {data && (
            <p className="leading-[1.4] font-semibold lg:hidden">
              {data.total} di {data.maximum} mazzi
            </p>
          )}

          {!noDeck && (
            <div className="relative z-30 flex flex-wrap gap-2">
              {FILTERS.map(({ key, label, title, single }) => (
                <Filter
                  key={key}
                  label={label}
                  title={title}
                  single={single}
                  options={options?.[key]}
                  chosen={filters[key]}
                  isOpen={openFilter === key}
                  onOpen={() => setOpenFilter(key)}
                  onClose={() => setOpenFilter(null)}
                  onApply={(values) => {
                    setFilters({ ...filters, [key]: values })
                    setOpenFilter(null)
                  }}
                />
              ))}
            </div>
          )}

          {!noDeck && (
            <LinkButton
              to="/collection/decks/new"
              aria-disabled={isFull}
              onClick={(event) => isFull && event.preventDefault()}
              className={`w-full lg:ml-auto lg:w-auto lg:shadow-[0_6px_8px_rgba(0,0,0,0.15)] ${isFull ? 'pointer-events-none opacity-50' : ''}`}
            >
              + Crea mazzo
            </LinkButton>
          )}
        </div>

        {openFilter && (
          <button
            type="button"
            aria-label="Chiudi il menu"
            onClick={() => setOpenFilter(null)}
            className="fixed inset-0 z-20 bg-[rgba(34,37,44,0.6)] opacity-60"
          />
        )}

        {/* 💻 Desktop: counter above the grid */}
        {data && !noDeck && (
          <p className="hidden leading-[1.4] font-semibold lg:block">
            {data.total} di {data.maximum} mazzi
            {!isFull && ` · puoi crearne ancora ${data.maximum - data.total}`}
          </p>
        )}

        {loading && (
          <div className="grid grid-cols-3 gap-x-2.5 gap-y-5 lg:flex lg:flex-wrap">
            {[1, 2, 3].map((n) => (
              <Skeleton key={n} className="h-58 rounded-2xl lg:w-35" />
            ))}
          </div>
        )}
        {error && <ErrorMessage />}

        {data?.decks.length > 0 && (
          <ul className="grid grid-cols-3 gap-x-2.5 gap-y-5 lg:flex lg:flex-wrap lg:gap-5">
            {data.decks.map((deck) => (
              <li key={deck.id}>
                <MatchDeck deck={deck} path={`/collection/decks/${deck.id}`} />
              </li>
            ))}
          </ul>
        )}

        {/* There are decks, but none matches the filters */}
        {data && !noDeck && data.decks.length === 0 && <p>Nessun mazzo corrisponde ai filtri scelti.</p>}

        {noDeck && <NoDeck />}

        {data && !noDeck && (
          <p className="text-center text-caption lg:hidden">Puoi avere fino a {data.maximum} mazzi.</p>
        )}
      </div>
    </AppLayout>
  )
}

/**
 * 5p · You have no deck yet
 *
 * @returns {JSX.Element}
 */
function NoDeck() {
  return (
    <div className="flex flex-col items-center gap-3 rounded-2xl border border-gold bg-background px-5 py-7 text-center lg:max-w-md">
      <span aria-hidden="true" className="flex size-20 items-center justify-center rounded-full border-2 border-gold bg-background-secondary font-title text-h2">
        🂠
      </span>
      <h2 className="font-title text-[22px]">Non hai ancora un mazzo</h2>
      <p>Un mazzo ha 30 carte di 2 Echi e costa al massimo 98 punti. Ti serve per giocare.</p>
      <LinkButton to="/collection/decks/new" className="w-full">
        Crea il tuo primo mazzo
      </LinkButton>
    </div>
  )
}
