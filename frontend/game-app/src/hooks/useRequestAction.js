import { useState } from 'react'
import { useNavigate } from 'react-router'
import { acceptRequest, declineRequest } from '../api/requests'

/**
 * Logic shared by Requests and Friends: accept / decline and show a message.
 * For CHALLENGES "Accetta" (accept) leads to the deck choice.
 *
 * @param {Function} reload - reloads the list after an answer
 * @returns {object}
 */
export function useRequestAction(reload) {
  const navigate = useNavigate()
  const [message, setMessage] = useState(null) // { kind: 'success' | 'info' | 'error', text }

  /** Accepts a request (a challenge goes to the deck choice first). */
  async function accept(request) {
    if (request.kind === 'challenge') {
      navigate(`/requests/${request.id}/deck?from=${encodeURIComponent(request.from.name)}`)
      return
    }
    try {
      await acceptRequest(request.id)
      setMessage({ kind: 'success', text: `${request.from.name} è ora tuo amico. Lo trovi in Amici.` })
      reload()
    } catch {
      setMessage({ kind: 'error', text: 'Operazione non riuscita. Riprova.' })
    }
  }

  /** Declines a request and shows a message. */
  async function decline(request) {
    try {
      await declineRequest(request.id)
      setMessage({ kind: 'info', text: `Hai rifiutato la richiesta di ${request.from.name}. Non riceverà nessuna notifica.` })
      reload()
    } catch {
      setMessage({ kind: 'error', text: 'Operazione non riuscita. Riprova.' })
    }
  }

  return { message, accept, decline }
}
