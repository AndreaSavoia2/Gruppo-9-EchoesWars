import { Link } from 'react-router'

// Il grande bottone GIOCA: porta alla scelta della modalità (/gioca).
// Su desktop ha un'immagine decorativa leggerissima dietro.
export default function BottoneGioca({ className = '' }) {
  return (
    <Link
      to="/gioca"
      className={`relative flex min-h-60 flex-col items-center justify-center gap-2.5 overflow-hidden rounded-[28px] border-2 border-oro-bordo bg-oro hover:brightness-95 ${className}`}
    >
      <img
        src="/images/card-gioca.png"
        alt=""
        className="absolute inset-0 hidden size-full object-cover opacity-5 lg:block"
      />
      {/* "relative" mette i testi SOPRA l'immagine */}
      <span className="relative text-[45px] font-bold">GIOCA</span>
      <span className="relative text-sottotitolo font-medium">Scegli lo sfidante e il mazzo</span>
    </Link>
  )
}
