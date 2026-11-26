import { useState } from 'react'
import { Link } from 'react-router'
import AppLayout from '../components/AppLayout'
import PageHeader from '../components/PageHeader'
import Notice from '../components/Notice'
import Button from '../components/Button'
import ErrorMessage from '../components/ErrorMessage'
import { changePassword } from '../api/profile'
import { clearToken, goToLogin } from '../api/token'

// The backend wants at least 12 characters (PasswordPolicy)
const MIN_LENGTH = 12

/**
 * /profile/password · Change password.
 * Asks for the current password and the new one (written twice to avoid typos).
 * After the change the backend closes all the sessions: the user has to log in again.
 *
 * @returns {JSX.Element}
 */
export default function ChangePassword() {
  const [fields, setFields] = useState({ current: '', newPassword: '', repeat: '' })
  const [status, setStatus] = useState({ inProgress: false, error: null, isDone: false })

  // Checks done straight away in the browser, before bothering the backend
  const tooShort = fields.newPassword.length > 0 && fields.newPassword.length < MIN_LENGTH
  const different = fields.repeat.length > 0 && fields.repeat !== fields.newPassword
  const equal = fields.newPassword.length > 0 && fields.newPassword === fields.current
  const ready =
    fields.current.length > 0 && fields.newPassword.length >= MIN_LENGTH && fields.repeat === fields.newPassword && !equal

  /**
   * Updates a form field.
   * @param {'current'|'newPassword'|'repeat'} name - which field
   * @param {string} value - the typed text
   * @returns {void}
   */
  function updateField(name, value) {
    setFields({ ...fields, [name]: value })
  }

  /**
   * Sends the password change to the backend.
   * @param {Event} event - the form submission
   * @returns {Promise<void>}
   */
  async function send(event) {
    event.preventDefault()
    if (!ready) return
    setStatus({ inProgress: true, error: null, isDone: false })
    try {
      await changePassword(fields.current, fields.newPassword)
      clearToken() // the backend closed the session: the token is no longer valid
      setStatus({ inProgress: false, error: null, isDone: true })
    } catch (error) {
      setStatus({ inProgress: false, error: errorMessage(error), isDone: false })
    }
  }

  if (status.isDone) {
    return (
      <AppLayout mobileBackground={null}>
        <div className="flex flex-col gap-4 lg:max-w-xl">
          <PageHeader title="Password cambiata" subtitle="Per sicurezza abbiamo chiuso tutte le sessioni aperte." />
          <Notice kind="success">La nuova password è attiva. Rifai l’accesso con la nuova password.</Notice>
          <Button
            onClick={() => {
              if (!goToLogin('password')) window.location.href = '/'
            }}
            className="w-full lg:min-h-12 lg:w-auto lg:min-w-50 lg:self-start"
          >
            Vai all’accesso
          </Button>
        </div>
      </AppLayout>
    )
  }

  return (
    <AppLayout mobileBackground={null}>
      <form onSubmit={send} noValidate className="flex flex-col gap-4 lg:max-w-xl">
        <PageHeader title="Cambia password" subtitle="Serve la password attuale." />
        <Link to="/profile" className="self-start font-medium underline">
          ‹ Profilo
        </Link>

        <PasswordField
          id="password-attuale"
          label="Password attuale"
          value={fields.current}
          onTextChange={(value) => updateField('current', value)}
          autocomplete="current-password"
        />
        <PasswordField
          id="password-nuova"
          label="Nuova password"
          hint={`Almeno ${MIN_LENGTH} caratteri.`}
          value={fields.newPassword}
          onTextChange={(value) => updateField('newPassword', value)}
          autocomplete="new-password"
          error={
            tooShort
              ? `La password deve avere almeno ${MIN_LENGTH} caratteri.`
              : equal
                ? 'La nuova password deve essere diversa da quella attuale.'
                : null
          }
        />
        <PasswordField
          id="password-ripeti"
          label="Ripeti la nuova password"
          value={fields.repeat}
          onTextChange={(value) => updateField('repeat', value)}
          autocomplete="new-password"
          error={different ? 'Le due password non sono uguali.' : null}
        />

        <Notice kind="info" icon="🔒">
          Dopo il cambio dovrai rifare l’accesso su tutti i dispositivi.
        </Notice>

        <Button type="submit" disabled={!ready || status.inProgress} className="w-full lg:min-h-12 lg:w-auto lg:min-w-50 lg:self-start">
          {status.inProgress ? 'Salvataggio…' : 'Cambia password'}
        </Button>
        {status.error && <ErrorMessage text={status.error} />}
      </form>
    </AppLayout>
  )
}

/**
 * A password field with label, help text and error message.
 * The "Mostra" (show) button reveals what is being typed.
 *
 * @param {object} props - the component props
 * @param {string} props.id - id of the field (links label and messages)
 * @param {string} props.label - text above the field
 * @param {string} props.value - typed text
 * @param {Function} props.onTextChange - called with the new text
 * @param {string} props.autocomplete - helps the browser suggest/save the password
 * @param {string} [props.hint] - hint under the field
 * @param {string|null} [props.error] - error message (red)
 * @returns {JSX.Element}
 */
function PasswordField({ id, label, value, onTextChange, autocomplete, hint, error }) {
  const [visible, setVisible] = useState(false)
  // aria-describedby: the screen reader also reads hint and error
  const description = [hint && `${id}-hint`, error && `${id}-error`].filter(Boolean).join(' ') || undefined

  return (
    <div className="flex flex-col gap-1.5">
      <label htmlFor={id} className="font-semibold">
        {label}
      </label>
      <div className="flex gap-2">
        <input
          id={id}
          type={visible ? 'text' : 'password'}
          value={value}
          onChange={(event) => onTextChange(event.target.value)}
          autoComplete={autocomplete}
          aria-invalid={Boolean(error)}
          aria-describedby={description}
          className={`min-h-12 min-w-0 flex-1 rounded-[14px] border-[1.5px] bg-background px-3.5 ${
            error ? 'border-error' : 'border-ink'
          }`}
        />
        <button
          type="button"
          onClick={() => setVisible(!visible)}
          aria-pressed={visible}
          aria-label={visible ? `Nascondi ${label.toLowerCase()}` : `Mostra ${label.toLowerCase()}`}
          className="min-h-12 shrink-0 rounded-[14px] border-[1.5px] border-ink px-3.5 font-medium hover:bg-background-secondary"
        >
          {visible ? 'Nascondi' : 'Mostra'}
        </button>
      </div>
      {hint && (
        <p id={`${id}-hint`} className="text-caption text-ink-secondary">
          {hint}
        </p>
      )}
      {error && (
        <p id={`${id}-error`} className="text-caption font-medium text-error">
          {error}
        </p>
      )}
    </div>
  )
}

/**
 * Turns the backend error into an understandable message (in Italian, shown to the user).
 * @param {Error & { status?: number }} error
 * @returns {string}
 */
function errorMessage(error) {
  if (error.status === 401) return 'La password attuale non è corretta.'
  if (error.status === 400 && /differ/i.test(error.message ?? '')) return 'La nuova password deve essere diversa da quella attuale.'
  if (error.status === 400) return `La nuova password non è valida: servono almeno ${MIN_LENGTH} caratteri.`
  return 'Cambio password non riuscito. Riprova più tardi.'
}
