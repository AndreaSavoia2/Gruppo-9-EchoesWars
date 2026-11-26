import { initials } from '../../utils/text'

/**
 * Gold circle with the initials (e.g. "@lyra_eco" → "LE")
 *
 * @param {object} props - the component props
 * @param {string} props.name
 * @returns {JSX.Element}
 */
export default function InitialsAvatar({ name }) {
  return (
    <span aria-hidden="true" className="flex size-11 shrink-0 items-center justify-center rounded-full bg-gold leading-[1.4] font-semibold">
      {initials(name)}
    </span>
  )
}
