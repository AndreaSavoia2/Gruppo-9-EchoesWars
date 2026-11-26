import { useState } from 'react'
import { Link, useNavigate } from 'react-router'
import AppLayout from '../components/AppLayout'
import PageHeader from '../components/PageHeader'
import Notice from '../components/Notice'
import Button from '../components/Button'
import Skeleton from '../components/Skeleton'
import ErrorMessage from '../components/ErrorMessage'
import { useFetch } from '../hooks/useFetch'
import { useUser } from '../context/UserContext'
import { getAvatar, saveAvatar } from '../api/profile'

/**
 * 11b / D11b · /profile/avatar
 *
 * @returns {JSX.Element}
 */
export default function EditAvatar() {
  const navigate = useNavigate()
  const { data: avatar, loading, error } = useFetch(getAvatar)
  const { reload: reloadUser } = useUser()
  const [selectedId, setSelectedId] = useState(null) // null = you have not touched anything yet
  const [saving, setSaving] = useState({ inProgress: false, error: null })

  const inUse = avatar?.find((a) => a.inUse)?.id
  const selected = selectedId ?? inUse

  /** Saves the chosen avatar. */
  async function save() {
    setSaving({ inProgress: true, error: null })
    try {
      await saveAvatar(selected)
      reloadUser() // the new avatar appears straight away in the top bar
      navigate('/profile')
    } catch {
      setSaving({ inProgress: false, error: 'Salvataggio non riuscito. Riprova.' })
    }
  }

  return (
    <AppLayout mobileBackground={null}>
      <div className="flex flex-col gap-2.5 lg:gap-4">
        <PageHeader title="Modifica avatar" subtitle="Scegli l’immagine che vedi in alto, accanto al tuo nome." />
        <Link to="/profile" className="mt-3 self-start font-medium underline lg:mt-0">
          ‹ Profilo
        </Link>

        {loading && <Skeleton className="h-80 rounded-2xl" />}
        {error && <ErrorMessage />}

        {avatar && (
          <>
            <ul className="grid grid-cols-3 gap-x-7.75 gap-y-5 lg:grid-cols-[repeat(auto-fill,100px)] lg:gap-x-6">
              {avatar.map((entry) => {
                const chosenOne = entry.id === selected
                return (
                  <li key={entry.id}>
                    <button
                      type="button"
                      onClick={() => setSelectedId(entry.id)}
                      disabled={!entry.available}
                      aria-pressed={chosenOne}
                      className="flex w-full flex-col items-center gap-1.5 text-center leading-[1.4] disabled:cursor-not-allowed"
                    >
                      <span
                        className={`relative flex size-22 items-center justify-center rounded-full ${
                          chosenOne ? 'border-4 border-ink bg-gold' : 'border-[1.5px] border-gold-border'
                        } ${entry.available ? 'bg-gold' : 'bg-background-secondary'}`}
                      >
                        {entry.image && entry.available && <img src={entry.image} alt="" className="size-16 object-contain" />}
                        {!entry.available && (
                          <span aria-hidden="true" className="flex size-16 items-center justify-center bg-gold/35 font-title text-[22px]">
                            🔒
                          </span>
                        )}
                        {chosenOne && (
                          <span aria-hidden="true" className="absolute -right-1 -bottom-1 flex size-7 items-center justify-center rounded-full bg-ink font-semibold text-background">
                            ✓
                          </span>
                        )}
                      </span>
                      <span className="font-semibold">{entry.name}</span>
                      <span className="text-caption font-medium">
                        {chosenOne ? '✓ In uso' : entry.available ? 'Disponibile' : entry.unlock}
                      </span>
                    </button>
                  </li>
                )
              })}
            </ul>

            <Notice kind="info" icon="🔒">
              Gli avatar bloccati si sbloccano con gli accessi consecutivi e le Prove degli Echi.
            </Notice>
            <Button onClick={save} disabled={selected === inUse || saving.inProgress} className="w-full lg:min-h-12 lg:w-auto lg:min-w-35 lg:self-start">
              {saving.inProgress ? 'Salvataggio…' : 'Salva avatar'}
            </Button>
            {saving.error && <ErrorMessage text={saving.error} />}
          </>
        )}
      </div>
    </AppLayout>
  )
}
