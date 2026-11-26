import { useCallback, useEffect, useState } from 'react'
import AppLayout from '../components/AppLayout'
import Button from '../components/Button'
import Skeleton from '../components/Skeleton'
import ErrorMessage from '../components/ErrorMessage'
import CollectionTabs from '../components/collection/CollectionTabs'
import Filter from '../components/collection/Filter'
import CollectionCard from '../components/collection/CollectionCard'
import { useFetch } from '../hooks/useFetch'
import { getCollection, getCollectionFilters } from '../api/collection'

// The 4 filters: "key" = name used with the backend, "label" = text on the button
const FILTERS = [
  { key: 'school', label: 'Scuola' },
  { key: 'echo', label: 'Eco' },
  { key: 'rarity', label: 'Rarità' },
  { key: 'ability', label: 'Abilità' },
]
const NO_FILTERS = { school: [], echo: [], rarity: [], ability: [] }

/**
 * 5 · 5f-5j · 5o / D3 · /collection — the owned cards, with the filters
 *
 * @returns {JSX.Element}
 */
export default function Collection() {
  const [filters, setFilters] = useState(NO_FILTERS)
  const [openFilter, setOpenFilter] = useState(null) // e.g. 'school', or null

  // When the filters change, the function changes → useFetch repeats the request
  const loadCards = useCallback((signal) => getCollection(filters, signal), [filters])
  const { data, loading, error } = useFetch(loadCards)
  const { data: options } = useFetch(getCollectionFilters)

  const activeFilters = Object.values(filters).some((values) => values.length > 0)

  // Esc key = close the menu of the open filter
  useEffect(() => {
    if (!openFilter) return
    const onKey = (event) => event.key === 'Escape' && setOpenFilter(null)
    window.addEventListener('keydown', onKey)
    return () => window.removeEventListener('keydown', onKey)
  }, [openFilter])

  /** Applies the values chosen in one filter. */
  function apply(key, values) {
    setFilters({ ...filters, [key]: values })
    setOpenFilter(null)
  }

  /** Removes all the filters. */
  function resetAll() {
    setFilters(NO_FILTERS)
  }

  return (
    <AppLayout activePage="collection" mobileBackground={null} desktopBackground="/images/collection-background.png">
      <div className="flex flex-col gap-3 lg:gap-6">
        {/* Header: on mobile with the library image behind */}
        <div className="relative -mx-5 -mt-8 flex h-27.5 flex-col justify-end gap-1 px-5 pb-4 lg:mx-0 lg:mt-0 lg:h-auto lg:gap-1.5 lg:p-0">
          <div aria-hidden="true" className="absolute inset-0 -z-10 lg:hidden">
            <img src="/images/collection-background.png" alt="" className="size-full object-cover" />
            <div className="absolute inset-0 bg-linear-to-b from-background/5 via-background/75 via-55% to-background" />
          </div>
          <h1 className="font-title text-h2">Collezione</h1>
          {data ? (
            <p className="text-ink-secondary lg:text-ink">{data.total} carte nella tua collezione</p>
          ) : (
            <Skeleton className="h-4 w-56" />
          )}
        </div>

        <div className="flex flex-col gap-3 pt-3 lg:flex-row lg:items-center lg:gap-6 lg:pt-0">
          <CollectionTabs active="cards" />

          {/* Filters. "z-30" keeps them above the dark veil when a menu is open */}
          <div className="relative z-30 flex flex-wrap gap-2">
            {FILTERS.map(({ key, label }) => (
              <Filter
                key={key}
                label={label}
                options={options?.[key]}
                chosen={filters[key]}
                isOpen={openFilter === key}
                onOpen={() => setOpenFilter(key)}
                onClose={() => setOpenFilter(null)}
                onApply={(values) => apply(key, values)}
              />
            ))}
          </div>
        </div>

        {/* Dark veil behind the open menu: tap it to close */}
        {openFilter && (
          <button
            type="button"
            aria-label="Chiudi il menu"
            onClick={() => setOpenFilter(null)}
            className="fixed inset-0 z-20 bg-[rgba(34,37,44,0.6)] opacity-60"
          />
        )}

        {activeFilters && data && (
          <div className="flex items-center justify-between">
            <p>{data.foundCards} carte trovate</p>
            <button type="button" onClick={resetAll} className="flex items-center gap-1 font-medium">
              <span aria-hidden="true">✕</span> Azzera filtri
            </button>
          </div>
        )}

        {loading && (
          <div className="grid grid-cols-3 gap-3 lg:grid-cols-[repeat(auto-fill,128px)] lg:gap-5">
            {[1, 2, 3, 4, 5, 6].map((n) => (
              <Skeleton key={n} className="aspect-[107/150] rounded-lg" />
            ))}
          </div>
        )}
        {error && <ErrorMessage />}

        {data?.cardList.length > 0 && (
          <ul className="grid grid-cols-3 gap-3 lg:grid-cols-[repeat(auto-fill,128px)] lg:gap-5">
            {data.cardList.map((card) => (
              <li key={card.id}>
                <CollectionCard card={card} />
              </li>
            ))}
          </ul>
        )}

        {data?.cardList.length === 0 && <NoResults onReset={resetAll} activeFilters={activeFilters} />}
      </div>
    </AppLayout>
  )
}

/**
 * 5o · No card found
 *
 * @param {object} props - the component props
 * @param {Function} props.onReset
 * @param {boolean} props.activeFilters
 * @returns {JSX.Element}
 */
function NoResults({ onReset, activeFilters }) {
  return (
    <div className="flex flex-col items-center gap-3 rounded-2xl border border-gold bg-background p-5 text-center lg:max-w-md">
      <span aria-hidden="true" className="flex size-16 items-center justify-center rounded-full border border-gold bg-gold/30 text-2xl">
        🔍
      </span>
      <h2 className="font-title text-[22px]">Nessuna carta trovata</h2>
      <p>
        {activeFilters
          ? 'Nessuna carta della tua collezione corrisponde ai filtri scelti.'
          : 'La tua collezione è ancora vuota.'}
      </p>
      {activeFilters && (
        <Button variant="secondary" onClick={onReset}>
          Azzera filtri
        </Button>
      )}
    </div>
  )
}
