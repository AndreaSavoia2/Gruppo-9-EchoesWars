import { useCallback, useState } from 'react'
import AppLayout from '../components/AppLayout'
import PageHeader from '../components/PageHeader'
import Tabs from '../components/Tabs'
import Notice from '../components/Notice'
import Modal from '../components/Modal'
import Button from '../components/Button'
import Skeleton from '../components/Skeleton'
import ErrorMessage from '../components/ErrorMessage'
import InitialsAvatar from '../components/friends/InitialsAvatar'
import RequestCard from '../components/friends/RequestCard'
import { useFetch } from '../hooks/useFetch'
import { useRequestAction } from '../hooks/useRequestAction'
import { addFriend, removeFriend, getFriendList } from '../api/friends'
import { getRequests } from '../api/requests'

/**
 * 9 · 9b · 9c / D8 · /friends
 *
 * @returns {JSX.Element}
 */
export default function Friends() {
  const { data: friends, loading, error, reload } = useFetch(getFriendList)
  const { data: requests, reload: reloadRequests } = useFetch(getRequests)
  const friendRequests = requests?.filter((r) => r.kind === 'friendship') ?? []

  // When you accept a friendship both the friends and the requests change
  const reloadAll = useCallback(() => {
    reload()
    reloadRequests()
  }, [reload, reloadRequests])
  const actions = useRequestAction(reloadAll)

  const [name, setName] = useState('')
  const [addOutcome, setAddOutcome] = useState(null)
  const [toDelete, setToDelete] = useState(null)
  const closeDelete = useCallback(() => setToDelete(null), [])

  /** Sends a friend request to the typed name. */
  async function add(event) {
    event.preventDefault() // the form must not reload the page
    const cleaned = name.trim()
    if (!cleaned) return
    const withAt = cleaned.startsWith('@') ? cleaned : `@${cleaned}`
    try {
      await addFriend(withAt)
      setAddOutcome({ kind: 'success', text: `Richiesta di amicizia inviata a ${withAt}.` })
      setName('')
    } catch {
      setAddOutcome({ kind: 'error', text: `Non abbiamo trovato ${withAt}. Controlla il nome utente.` })
    }
  }

  /** Removes the friend chosen in the confirmation window. */
  async function remove() {
    await removeFriend(toDelete.id)
    setToDelete(null)
    reload()
  }

  const friendCount = friends?.length ?? 0
  const subtitle = friends && requests ? `${friendCount} amici · ${friendRequests.length} richieste` : ' '

  const addForm = (
    <form onSubmit={add} className="flex items-center gap-2">
      <label className="sr-only" htmlFor="cerca-amico">
        Nome utente da aggiungere
      </label>
      <input
        id="cerca-amico"
        type="text"
        value={name}
        onChange={(event) => setName(event.target.value)}
        placeholder="Cerca per nome utente"
        className="min-h-12 min-w-0 flex-1 rounded-[14px] border-[1.5px] border-ink bg-background px-3.5 placeholder:text-neutral"
      />
      <Button type="submit" className="min-h-12 w-28 shrink-0">
        Aggiungi
      </Button>
    </form>
  )

  return (
    <AppLayout activePage="friends">
      <div className="flex flex-col gap-2.5 lg:gap-6">
        <PageHeader title="Amici" subtitle={subtitle} />

        <div className="flex flex-col gap-6 lg:flex-row lg:items-start">
          <section className="flex flex-1 flex-col gap-2.5 pt-3 lg:rounded-2xl lg:border lg:border-gold lg:bg-background lg:p-4 lg:pt-4">
            <h2 className="hidden font-title text-h2 lg:block">I tuoi amici ({friendCount})</h2>
            {addForm}
            {addOutcome && <Notice kind={addOutcome.kind}>{addOutcome.text}</Notice>}

            {/* 📱 Mobile: Friends / Requests tabs */}
            <div className="lg:hidden">
              <Tabs
                tabs={[
                  { id: 'friends', label: `Amici (${friendCount})`, path: '/friends' },
                  { id: 'richieste', label: `Richieste (${friendRequests.length})`, path: '/requests?tab=friendship' },
                ]}
                active="friends"
                label="Amici e richieste"
              />
            </div>

            {loading && <Skeleton className="h-60 rounded-2xl" />}
            {error && <ErrorMessage />}

            {friends?.length === 0 && (
              <div className="flex flex-col items-center gap-3 rounded-2xl border border-gold bg-background px-5 py-6 text-center">
                <span aria-hidden="true" className="flex size-16 items-center justify-center rounded-full border-2 border-gold bg-background-secondary text-2xl">
                  👥
                </span>
                <h3 className="font-title text-[22px]">Non hai ancora amici</h3>
                <p>Scrivi il nome utente di un giocatore nel campo qui sopra e premi Aggiungi. Con gli amici puoi sfidarti e scambiare carte.</p>
              </div>
            )}

            {friends?.length > 0 && (
              <ul className="flex flex-col gap-2.5">
                {friends.map((friend) => (
                  <li key={friend.id} className="flex items-center gap-3 rounded-2xl border border-gold-border bg-background py-3 pr-2.5 pl-3.5">
                    <InitialsAvatar name={friend.name} />
                    <span className="flex-1 truncate leading-[1.4] font-semibold">{friend.name}</span>
                    <button
                      type="button"
                      onClick={() => setToDelete(friend)}
                      aria-label={`Elimina ${friend.name} dagli amici`}
                      className="flex size-11 shrink-0 items-center justify-center rounded-full border-[1.5px] border-error hover:bg-error-background"
                    >
                      <img src="/icons/trash.svg" alt="" className="size-6" />
                    </button>
                  </li>
                ))}
              </ul>
            )}
          </section>

          {/* 💻 Desktop: friend requests on the right */}
          <aside className="hidden w-100 shrink-0 flex-col gap-3 rounded-2xl border border-gold bg-background p-4 lg:flex">
            <h2 className="font-title text-h2">Richieste ({friendRequests.length})</h2>
            {actions.message && <Notice kind={actions.message.kind}>{actions.message.text}</Notice>}
            {friendRequests.length === 0 && <p>Nessuna richiesta di amicizia.</p>}
            {friendRequests.map((request) => (
              <RequestCard
                key={request.id}
                request={request}
                compact
                onAccept={() => actions.accept(request)}
                onDecline={() => actions.decline(request)}
              />
            ))}
          </aside>
        </div>
      </div>

      {/* 9b · Remove friend */}
      {toDelete && (
        <Modal title={`Eliminare ${toDelete.name} dagli amici?`} onClose={closeDelete}>
          <p className="text-ink-secondary">
            Non potrete più sfidarvi né scambiarvi carte. Potrai inviargli di nuovo una richiesta.
          </p>
          <Button variant="destructive" onClick={remove}>
            Elimina amico
          </Button>
          <Button variant="secondary" onClick={closeDelete}>
            Annulla
          </Button>
        </Modal>
      )}
    </AppLayout>
  )
}
