import AvatarIniziali from './AvatarIniziali'
import Bottone from '../Bottone'
import { quantoFa } from '../../utils/testo'

const tipi = {
  amicizia: { testo: 'Vuole diventare tuo amico', etichetta: '👥 Amicizia', stile: 'border-oro bg-sfondo-secondario', accetta: 'Accetta' },
  sfida: { testo: 'Ti sfida a una partita', etichetta: '⚔ Sfida', stile: 'border-info bg-info-sfondo', accetta: 'Accetta e scegli il mazzo' },
}

/**
 * Una richiesta (dal Figma "App/Richiesta"). Il tipo è scritto, non solo colorato.
 * compatta = true → versione piccola del pannello a destra in Amici (desktop)
 *
 * @param {object} props - le proprietà del componente
 * @param {*} props.richiesta
 * @param {Function} props.onAccetta
 * @param {Function} props.onRifiuta
 * @param {boolean} [props.compatta=false]
 * @returns {JSX.Element}
 */
export default function CartaRichiesta({ richiesta, onAccetta, onRifiuta, compatta = false }) {
  const tipo = tipi[richiesta.tipo]
  return (
    <article className="flex flex-col gap-3 rounded-2xl border border-oro-bordo bg-sfondo p-3.5">
      <div className="flex items-center gap-3">
        <AvatarIniziali nome={richiesta.da.nome} />
        <div className="flex min-w-0 flex-1 flex-col gap-0.5 leading-[1.4]">
          <h3 className="truncate font-semibold">{richiesta.da.nome}</h3>
          <p className="text-didascalia font-medium">{tipo.testo}</p>
        </div>
        {!compatta && (
          <span className={`shrink-0 rounded-full border px-2.5 py-1 text-didascalia leading-[1.4] font-medium ${tipo.stile}`}>
            {tipo.etichetta}
          </span>
        )}
      </div>
      {!compatta && <p className="text-didascalia leading-[1.4] font-medium">{quantoFa(richiesta.ricevutaIl)}</p>}
      <div className="flex gap-2.5">
        <Bottone onClick={onAccetta} className="flex-1">
          {tipo.accetta}
        </Bottone>
        <Bottone variante="secondario" onClick={onRifiuta} className={richiesta.tipo === 'sfida' ? 'w-auto' : 'flex-1'}>
          Rifiuta
        </Bottone>
      </div>
    </article>
  )
}
