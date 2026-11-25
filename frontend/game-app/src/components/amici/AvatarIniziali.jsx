import { iniziali } from '../../utils/testo'

/**
 * Cerchio oro con le iniziali (es. "@lyra_eco" → "LE")
 *
 * @param {object} props - le proprietà del componente
 * @param {string} props.nome
 * @returns {JSX.Element}
 */
export default function AvatarIniziali({ nome }) {
  return (
    <span aria-hidden="true" className="flex size-11 shrink-0 items-center justify-center rounded-full bg-oro leading-[1.4] font-semibold">
      {iniziali(nome)}
    </span>
  )
}
