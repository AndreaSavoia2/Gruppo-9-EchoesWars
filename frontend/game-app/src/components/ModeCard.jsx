import { Link } from 'react-router'

/**
 * Big card to choose the game mode (desktop version):
 * image, icon + title, description, 3 numbered steps, button.
 *
 * @param {object} props - the component props
 * @param {string} props.path
 * @param {string} props.image
 * @param {string} props.icon
 * @param {string} props.title
 * @param {string} props.description
 * @param {string[]} props.steps
 * @param {string} props.buttonText
 * @returns {JSX.Element}
 */
export default function ModeCard({ path, image, icon, title, description, steps, buttonText }) {
  return (
    <Link
      to={path}
      className="group flex flex-col overflow-hidden rounded-2xl border border-gold-border bg-background"
    >
      <img src={image} alt="" className="h-55 w-full object-cover" />

      <div className="flex flex-1 flex-col gap-4.5 p-7">
        <div className="flex items-center gap-3.5">
          <span className="flex size-13 shrink-0 items-center justify-center rounded-xl bg-gold">
            <img src={icon} alt="" className="size-6" />
          </span>
          <h2 className="font-title text-[36px]">{title}</h2>
        </div>

        <p className="text-ink-secondary">{description}</p>

        <hr className="border-gold-border/50" />

        <p className="leading-[1.4] font-semibold">Come funziona</p>

        {/* <ol> = numbered list: we draw the numbers ourselves inside the small circles */}
        <ol className="flex flex-col gap-3">
          {steps.map((step, index) => (
            <li key={step} className="flex items-center gap-3">
              <span className="flex size-7 shrink-0 items-center justify-center rounded-full bg-gold leading-[1.4] font-semibold">
                {index + 1}
              </span>
              {step}
            </li>
          ))}
        </ol>

        {/* It looks like a button, but it is part of the link: the whole card is clickable */}
        <span className="mt-auto flex h-12 items-center justify-center rounded-full bg-gold-border px-5 font-medium group-hover:brightness-95">
          {buttonText}
        </span>
      </div>
    </Link>
  )
}
