// Coloured notice box (from the Figma). kind: info · error · success
const styles = {
  info: { box: 'border-info bg-info-background', icon: 'i', color: 'text-info' },
  error: { box: 'border-error bg-error-background', icon: '⚠', color: 'text-error' },
  success: { box: 'border-success bg-success-background', icon: '✓', color: 'text-success' },
}

/**
 * withBar = true → thick border only on the left (as in Trades)
 *
 * @param {object} props - the component props
 * @param {'info'|'error'|'success'} [props.kind='info']
 * @param {string} [props.icon]
 * @param {boolean} [props.withBar=false]
 * @param {React.ReactNode} props.children
 * @returns {JSX.Element}
 */
export default function Notice({ kind = 'info', icon, withBar = false, children }) {
  const styleName = styles[kind]
  return (
    <p
      role={kind === 'error' ? 'alert' : undefined}
      className={`flex items-center gap-2.5 rounded-xl px-3.5 py-2.5 text-caption leading-[1.4] font-medium ${styleName.box} ${
        withBar ? 'border-l-4' : 'border'
      }`}
    >
      <span aria-hidden="true" className={`text-body font-semibold ${styleName.color}`}>
        {icon ?? styleName.icon}
      </span>
      <span className="flex-1">{children}</span>
    </p>
  )
}
