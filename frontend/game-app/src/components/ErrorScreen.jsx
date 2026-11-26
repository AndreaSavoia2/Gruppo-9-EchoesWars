import AppLayout from './AppLayout'

// Colours of the circle with the icon, one per kind of problem (from the Figma E1-E3)
const iconColors = {
  info: 'border-info bg-info-background', //       E1 · You are offline (purple)
  error: 'border-error bg-error-background', //  E2 · Server not reachable (red)
  notice: 'border-gold bg-background', //            E3 · Match interrupted (gold)
}

/**
 * Full screen for the "big" errors: title, explanation, icon and buttons at the bottom.
 * The buttons are passed by whoever uses it (children), because they change from error to error.
 *
 * Example:
 *   <ErrorScreen title="Sei offline" text="Controlla..." icon="📶" color="info">
 *     <Button onClick={retry}>Riprova</Button>
 *   </ErrorScreen>
 *
 * @param {object} props - the component props
 * @param {string} props.title
 * @param {string} props.text
 * @param {string} props.icon
 * @param {'info'|'error'|'notice'} [props.color='error']
 * @param {React.ReactNode} props.children
 * @returns {JSX.Element}
 */
export default function ErrorScreen({ title, text, icon, color = 'error', children }) {
  return (
    <AppLayout mobileBackground="/images/play-background.png" hideMobileMenu>
      {/* role="alert": the screen reader reads the message straight away */}
      <div role="alert" className="mx-auto flex w-full max-w-xl flex-1 flex-col items-center gap-6 text-center lg:justify-center">
        <div className="flex flex-col gap-3">
          <h1 className="font-title text-[22px] lg:text-h2">{title}</h1>
          <p>{text}</p>
        </div>

        {/* aria-hidden: the emoji is only decorative, the title already says everything */}
        <div
          aria-hidden="true"
          className={`mt-8 flex size-28 items-center justify-center rounded-full border-2 text-h2 ${iconColors[color]}`}
        >
          {icon}
        </div>

        {/* mt-auto: on mobile it pushes the buttons to the bottom of the screen */}
        <div className="mt-auto flex w-full flex-col gap-3 lg:mt-8 lg:flex-row lg:justify-center [&>*]:lg:w-auto [&>*]:lg:min-w-50">
          {children}
        </div>
      </div>
    </AppLayout>
  )
}
