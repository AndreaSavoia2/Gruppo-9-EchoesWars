import { useState } from 'react'
import { Link } from 'react-router'
import AppLayout from '../components/AppLayout'
import PageHeader from '../components/PageHeader'
import Notice from '../components/Notice'
import SelectableRow from '../components/SelectableRow'
import { LANGUAGES, chosenLanguage, saveLanguage } from '../utils/language'

/**
 * /profile/language · Language choice (Italian or English).
 * The choice is saved straight away, as soon as a language is tapped.
 *
 * @returns {JSX.Element}
 */
export default function Language() {
  const [choice, setChoice] = useState(chosenLanguage().code)

  /**
   * Chooses and saves a language.
   * @param {string} code - "it" or "en"
   * @returns {void}
   */
  function choose(code) {
    saveLanguage(code)
    setChoice(code)
  }

  const chosenName = LANGUAGES.find((language) => language.code === choice)?.name

  return (
    <AppLayout mobileBackground={null}>
      <div className="flex flex-col gap-4 lg:max-w-xl">
        <PageHeader title="Lingua" subtitle="Scegli la lingua dell’app." />
        <Link to="/profile" className="self-start font-medium underline">
          ‹ Profilo
        </Link>

        <ul className="flex flex-col gap-2.5">
          {LANGUAGES.map((language) => (
            <li key={language.code}>
              <SelectableRow
                title={language.originalName}
                detail={choice === language.code ? '✓ In uso' : language.name}
                selected={choice === language.code}
                onChoose={() => choose(language.code)}
                icon="/icons/globe.svg"
              />
            </li>
          ))}
        </ul>

        {/* aria-live: the screen reader announces the language as soon as it changes */}
        <p aria-live="polite" className="sr-only">
          Lingua scelta: {chosenName}
        </p>

        {choice !== 'it' && (
          <Notice kind="info" icon="🌍">
            La traduzione in inglese è in preparazione: per ora i testi dell’app restano in italiano.
          </Notice>
        )}
      </div>
    </AppLayout>
  )
}
