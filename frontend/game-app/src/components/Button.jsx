import { Link } from 'react-router'

// App button styles (from the Figma "App/Bottone"), at least 44px high.
//   primary     → solid gold: the main action (only one per screen)
//   secondary   → border only: alternative / cancel
//   destructive → red border and text: an action that cannot be undone (always with confirmation!)
const variants = {
  primary: 'bg-gold-border hover:brightness-95',
  secondary: 'border-[1.5px] border-ink hover:bg-background-secondary',
  destructive: 'border-[1.5px] border-error text-error hover:bg-error-background',
}
const base =
  'flex min-h-11 items-center justify-center rounded-full px-5 font-medium disabled:cursor-not-allowed disabled:opacity-50'

/**
 * <Button> = a real <button> (for actions: send, confirm...)
 *
 * @param {object} props - the component props
 * @param {string} [props.variant='primary']
 * @param {string} [props.className='w-full']
 * @param {React.ReactNode} props.children
 * @param {object} [props.otherProps] - all the other props
 * @returns {JSX.Element}
 */
export default function Button({ variant = 'primary', className = 'w-full', children, ...otherProps }) {
  return (
    <button className={`${base} ${variants[variant]} ${className}`} {...otherProps}>
      {children}
    </button>
  )
}

/**
 * <LinkButton> = looks like a button but goes to another page
 *
 * @param {object} props - the component props
 * @param {string} [props.variant='primary']
 * @param {string} [props.className='w-full']
 * @param {React.ReactNode} props.children
 * @param {object} [props.otherProps] - all the other props
 * @returns {JSX.Element}
 */
export function LinkButton({ variant = 'primary', className = 'w-full', children, ...otherProps }) {
  return (
    <Link className={`${base} ${variants[variant]} ${className}`} {...otherProps}>
      {children}
    </Link>
  )
}
