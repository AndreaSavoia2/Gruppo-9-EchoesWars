import { Link } from 'react-router'

/**
 * The big PLAY button: goes to the game mode choice (/play).
 * On desktop it has a very light decorative image behind.
 *
 * @param {object} props - the component props
 * @param {string} [props.className='']
 * @returns {JSX.Element}
 */
export default function PlayButton({ className = '' }) {
  return (
    <Link
      to="/play"
      className={`relative flex min-h-60 flex-col items-center justify-center gap-2.5 overflow-hidden rounded-[28px] border-2 border-gold-border bg-gold hover:brightness-95 ${className}`}
    >
      <img
        src="/images/play-card.png"
        alt=""
        className="absolute inset-0 hidden size-full object-cover opacity-5 lg:block"
      />
      {/* "relative" puts the texts ABOVE the image */}
      <span className="relative text-[45px] font-bold">GIOCA</span>
      <span className="relative text-subtitle font-medium">Scegli lo sfidante e il mazzo</span>
    </Link>
  )
}
