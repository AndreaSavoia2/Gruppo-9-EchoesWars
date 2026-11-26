import TopBar from './TopBar'
import SidebarNav from './SidebarNav'
import BottomNav from './BottomNav'

/**
 * The "shell" of every page of the app:
 * background + side menu (desktop) + top bar + bottom menu (mobile).
 * Every page puts its content inside with {children}.
 * mobileBackground / desktopBackground: some pages have a different background image.
 * hideMobileMenu: on some screens (e.g. deck choice) instead of the bottom
 * menu there is a bar with the action button.
 *
 * @param {object} props - the component props
 * @param {string} props.activePage - id of the current menu entry
 * @param {boolean} [props.hideMobileMenu=false]
 * @param {string|null} [props.mobileBackground='/images/app-background.png']
 * @param {string} [props.desktopBackground='/images/desktop-background.png']
 * @param {React.ReactNode} props.children
 * @returns {JSX.Element}
 */
export default function AppLayout({
  activePage,
  hideMobileMenu = false,
  mobileBackground = '/images/app-background.png',
  desktopBackground = '/images/desktop-background.png',
  children,
}) {
  return (
    <div className="relative isolate min-h-screen lg:flex">
      {/* Decorative background: different image for mobile and desktop + cream veil */}
      <div aria-hidden="true" className="fixed inset-0 -z-10">
        <picture>
          <source media="(min-width: 1024px)" srcSet={desktopBackground} />
          {/* mobileBackground = null → no image on mobile, only the cream colour */}
          <img
            src={mobileBackground ?? desktopBackground}
            alt=""
            className={`size-full object-cover ${mobileBackground ? '' : 'hidden lg:block'}`}
          />
        </picture>
        <div className="absolute inset-0 bg-background/92" />
      </div>

      <SidebarNav activePage={activePage} />

      <div className="flex min-h-screen flex-1 flex-col">
        <TopBar />
        {/* pb-28 on mobile: leaves room for the bottom menu */}
        <main className="flex flex-1 flex-col px-5 pt-8 pb-28 lg:px-12 lg:py-8">{children}</main>
      </div>

      {!hideMobileMenu && <BottomNav activePage={activePage} />}
    </div>
  )
}
