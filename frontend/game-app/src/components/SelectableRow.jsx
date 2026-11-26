/**
 * Row to choose (from the Figma "App/Opzione"): icon, title, detail, arrow.
 * Selected = gold background + thick border + check mark (not just the colour).
 *
 * @param {object} props - the component props
 * @param {string} props.title
 * @param {string} props.detail
 * @param {boolean} props.selected
 * @param {Function} props.onChoose
 * @param {string} [props.icon='/icons/person.svg']
 * @param {string} [props.desktopIcon=icon]
 * @returns {JSX.Element}
 */
export default function SelectableRow({ title, detail, selected, onChoose, icon = '/icons/person.svg', desktopIcon = icon }) {
  return (
    <button
      type="button"
      onClick={onChoose}
      aria-pressed={selected}
      className={`flex w-full items-center gap-3.5 rounded-2xl px-4 py-3.5 text-left ${
        selected ? 'border-2 border-ink bg-gold' : 'border border-gold-border bg-background hover:bg-background-secondary'
      }`}
    >
      <span className="flex size-11 shrink-0 items-center justify-center rounded-xl bg-gold">
        <img src={icon} alt="" className="size-6 lg:hidden" />
        <img src={desktopIcon} alt="" className="hidden size-6 lg:block" />
      </span>
      <span className="flex min-w-0 flex-1 flex-col gap-0.5 leading-[1.4]">
        <span className="truncate font-semibold">{title}</span>
        <span className="text-caption font-medium text-ink-secondary">{detail}</span>
      </span>
      <img src={selected ? '/icons/check.svg' : '/icons/arrow-right.svg'} alt="" className="size-6 shrink-0" />
    </button>
  )
}
