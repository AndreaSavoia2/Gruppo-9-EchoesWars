/**
 * Grey rectangle that "pulses" while the data is arriving.
 * className decides the size, e.g.: <Skeleton className="h-4 w-24" />
 *
 * @param {object} props - the component props
 * @param {string} [props.className='']
 * @returns {JSX.Element}
 */
export default function Skeleton({ className = '' }) {
  return <span aria-hidden="true" className={`block animate-pulse rounded bg-gold/50 ${className}`} />
}
