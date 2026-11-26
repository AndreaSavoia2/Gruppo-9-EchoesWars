import { useState } from 'react'
import { Link, useNavigate } from 'react-router'
import AppLayout from '../components/AppLayout'
import PageHeader from '../components/PageHeader'
import Button from '../components/Button'
import SelectableRow from '../components/SelectableRow'
import Skeleton from '../components/Skeleton'
import ErrorMessage from '../components/ErrorMessage'
import { useFetch } from '../hooks/useFetch'
import { getFriends, sendChallenge } from '../api/challenges'
import { timeAgo } from '../utils/text'

/**
 * A1 / DA1 · /play/friend — choose which friend to challenge
 *
 * @returns {JSX.Element}
 */
export default function ChooseFriend() {
  const navigate = useNavigate()
  const { data: friends, loading, error } = useFetch(getFriends)
  const [chosenId, setChosenId] = useState(null)
  const [sending, setSending] = useState({ inProgress: false, error: null })

  const chosenFriend = friends?.find((a) => a.id === chosenId)

  /** Sends the challenge to the chosen friend. */
  async function send() {
    setSending({ inProgress: true, error: null })
    try {
      const challenge = await sendChallenge(chosenFriend.id)
      // Request sent: now choose the deck while you wait
      navigate(`/challenge/${challenge.id}/deck`)
    } catch {
      setSending({ inProgress: false, error: 'Non è stato possibile inviare la richiesta. Riprova.' })
    }
  }

  return (
    <AppLayout activePage="play" mobileBackground="/images/play-background.png">
      <div className="flex flex-col gap-5 lg:gap-6">
        <PageHeader title="Sfida un amico" subtitle="Scegli chi sfidare.">
          <Link to="/play" className="hidden font-medium underline lg:block">
            ‹ Come vuoi giocare?
          </Link>
        </PageHeader>

        <section className="flex flex-col gap-2.5 lg:rounded-2xl lg:border lg:border-gold lg:bg-background lg:p-6">
          <h2 className="leading-[1.4] font-semibold lg:sr-only">I tuoi amici</h2>

          {loading && <Skeleton className="h-72 rounded-2xl" />}
          {error && <ErrorMessage />}
          {friends?.length === 0 && <p>Non hai ancora amici da sfidare. Aggiungili dalla sezione Amici.</p>}

          {friends?.length > 0 && (
            <ul className="flex flex-col gap-2.5 lg:grid lg:grid-cols-[repeat(auto-fill,minmax(360px,500px))] lg:gap-3">
              {friends.map((friend) => (
                <li key={friend.id}>
                  <SelectableRow
                    title={friend.name}
                    detail={friend.lastChallenge ? `Ultima sfida: ${timeAgo(friend.lastChallenge)}` : 'Mai sfidato'}
                    desktopIcon="/icons/swords.svg"
                    selected={friend.id === chosenId}
                    onChoose={() => setChosenId(friend.id)}
                  />
                </li>
              ))}
            </ul>
          )}
        </section>

        <Button onClick={send} disabled={!chosenFriend || sending.inProgress} className="w-full lg:min-h-12 lg:w-auto lg:self-start">
          {sending.inProgress ? 'Invio…' : chosenFriend ? `Invia la richiesta a ${chosenFriend.name}` : 'Scegli un amico'}
        </Button>
        {sending.error && <ErrorMessage text={sending.error} />}
      </div>
    </AppLayout>
  )
}
