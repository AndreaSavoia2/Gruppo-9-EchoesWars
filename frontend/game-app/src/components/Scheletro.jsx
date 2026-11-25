/**
 * Rettangolo grigio che "pulsa" mentre i dati stanno arrivando.
 * className decide la misura, es: <Scheletro className="h-4 w-24" />
 *
 * @param {object} props - le proprietà del componente
 * @param {string} [props.className='']
 * @returns {JSX.Element}
 */
export default function Scheletro({ className = '' }) {
  return <span aria-hidden="true" className={`block animate-pulse rounded bg-oro/50 ${className}`} />
}
