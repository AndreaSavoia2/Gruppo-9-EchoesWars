import TopBar from './TopBar'
import SidebarNav from './SidebarNav'
import BottomNav from './BottomNav'

/**
 * Il "guscio" di tutte le pagine dell'app:
 * sfondo + menu laterale (desktop) + barra in alto + menu in basso (mobile).
 * Ogni pagina ci mette dentro il suo contenuto con {children}.
 * sfondoMobile / sfondoDesktop: alcune pagine hanno un'immagine di sfondo diversa.
 * nascondiMenuMobile: su alcune schermate (es. scelta del mazzo) al posto del menu
 * in basso c'è una barra con il bottone dell'azione.
 *
 * @param {object} props - le proprietà del componente
 * @param {*} props.paginaAttiva
 * @param {boolean} [props.nascondiMenuMobile=false]
 * @param {string} [props.sfondoMobile='/images/sfondo-app.png']
 * @param {string} [props.sfondoDesktop='/images/sfondo-desktop.png']
 * @param {React.ReactNode} props.children
 * @returns {JSX.Element}
 */
export default function AppLayout({
  paginaAttiva,
  nascondiMenuMobile = false,
  sfondoMobile = '/images/sfondo-app.png',
  sfondoDesktop = '/images/sfondo-desktop.png',
  children,
}) {
  return (
    <div className="relative isolate min-h-screen lg:flex">
      {/* Sfondo decorativo: immagine diversa per mobile e desktop + velo crema */}
      <div aria-hidden="true" className="fixed inset-0 -z-10">
        <picture>
          <source media="(min-width: 1024px)" srcSet={sfondoDesktop} />
          {/* sfondoMobile = null → su mobile niente immagine, solo il colore crema */}
          <img
            src={sfondoMobile ?? sfondoDesktop}
            alt=""
            className={`size-full object-cover ${sfondoMobile ? '' : 'hidden lg:block'}`}
          />
        </picture>
        <div className="absolute inset-0 bg-sfondo/92" />
      </div>

      <SidebarNav paginaAttiva={paginaAttiva} />

      <div className="flex min-h-screen flex-1 flex-col">
        <TopBar />
        {/* pb-28 su mobile: lascia spazio al menu in basso */}
        <main className="flex flex-1 flex-col px-5 pt-8 pb-28 lg:px-12 lg:py-8">{children}</main>
      </div>

      {!nascondiMenuMobile && <BottomNav paginaAttiva={paginaAttiva} />}
    </div>
  )
}
