import { useState } from 'react'
import { Link } from 'react-router'
import AppLayout from '../components/AppLayout'
import Intestazione from '../components/Intestazione'
import Avviso from '../components/Avviso'
import RigaSelezionabile from '../components/RigaSelezionabile'
import { LINGUE, linguaScelta, salvaLingua } from '../utils/lingua'

/**
 * /profilo/lingua · Scelta della lingua (italiano o inglese).
 * La scelta viene salvata subito, appena si tocca una lingua.
 *
 * @returns {JSX.Element}
 */
export default function Lingua() {
  const [scelta, setScelta] = useState(linguaScelta().codice)

  /**
   * Sceglie e salva una lingua.
   * @param {string} codice - "it" oppure "en"
   */
  function scegli(codice) {
    salvaLingua(codice)
    setScelta(codice)
  }

  const nomeScelta = LINGUE.find((lingua) => lingua.codice === scelta)?.nome

  return (
    <AppLayout sfondoMobile={null}>
      <div className="flex flex-col gap-4 lg:max-w-xl">
        <Intestazione titolo="Lingua" sottotitolo="Scegli la lingua dell’app." />
        <Link to="/profilo" className="self-start font-medium underline">
          ‹ Profilo
        </Link>

        <ul className="flex flex-col gap-2.5">
          {LINGUE.map((lingua) => (
            <li key={lingua.codice}>
              <RigaSelezionabile
                titolo={lingua.nomeOriginale}
                dettaglio={scelta === lingua.codice ? '✓ In uso' : lingua.nome}
                scelto={scelta === lingua.codice}
                onScegli={() => scegli(lingua.codice)}
                icona="/icons/globo.svg"
              />
            </li>
          ))}
        </ul>

        {/* aria-live: lo screen reader annuncia la lingua appena cambia */}
        <p aria-live="polite" className="sr-only">
          Lingua scelta: {nomeScelta}
        </p>

        {scelta !== 'it' && (
          <Avviso tipo="info" icona="🌍">
            La traduzione in inglese è in preparazione: per ora i testi dell’app restano in italiano.
          </Avviso>
        )}
      </div>
    </AppLayout>
  )
}
