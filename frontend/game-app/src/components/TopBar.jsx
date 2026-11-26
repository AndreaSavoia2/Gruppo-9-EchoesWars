import { Link } from 'react-router'
import { useUser } from '../context/UserContext'
import Skeleton from './Skeleton'

// Formats numbers the Italian way with a dot: 1250 → "1.250"
// (useGrouping: 'always' is needed because in Italian, by default, 1250 has no dot)
const creditFormat = new Intl.NumberFormat('it-IT', { useGrouping: 'always' })

/**
 * Top bar: avatar, user name and credits.
 * Name and credits come from the backend (useUser).
 *
 * @returns {JSX.Element}
 */
export default function TopBar() {
  const { data: user, loading } = useUser()

  return (
    <header className="sticky top-0 z-10 flex h-18 items-center gap-3 border border-gold-border bg-background-secondary px-5">
      {/* Avatar: circle with an icon, goes to the profile */}
      <Link
        to="/profile"
        aria-label="Il tuo profilo"
        className="flex size-11 shrink-0 items-center justify-center rounded-full border-2 border-ink"
      >
        {user?.avatar ? (
          <img src={user.avatar} alt="" className="size-8 object-contain" />
        ) : (
          <img src="/icons/avatar.svg" alt="" className="size-6" />
        )}
      </Link>

      {/* flex-1 = "take all the free space" → pushes the credits to the right */}
      <div className="min-w-0 flex-1">
        {loading ? (
          <Skeleton className="h-4 w-28" />
        ) : (
          <p className="truncate font-medium">{user?.name}</p>
        )}
      </div>

      {/* Credits: we only show them once they have arrived */}
      {loading ? (
        <Skeleton className="h-11 w-24 rounded-full" />
      ) : (
        user && (
          <div className="flex min-h-11 shrink-0 items-center gap-1.5 rounded-full border border-ink bg-background-secondary px-3.5">
            <img src="/icons/credits.svg" alt="" className="size-4" />
            <span className="text-caption font-semibold">
              {creditFormat.format(user.credits)}
              <span className="sr-only"> crediti</span>
            </span>
          </div>
        )
      )}
    </header>
  )
}
