import CartaGioco from '../components/carta/CartaGioco'
import { CARTE_ESEMPIO } from '../api/demo'

/**
 * 🧪 Solo in modalità demo: /carte-demo
 * Mostra le carte di esempio a 3 grandezze diverse, per controllare
 * che cornice, illustrazione e testi restino al loro posto.
 *
 * @returns {JSX.Element}
 */
export default function VetrinaCarte() {
  return (
    <main className="min-h-screen bg-sfondo px-5 py-8 lg:px-12">
      <h1 className="font-titolo text-h2">Vetrina carte (demo)</h1>
      <p className="mb-6">Cornice + illustrazione + testi. Le carte senza illustrazione mostrano il segnaposto.</p>

      <h2 className="mb-3 font-semibold">Grandi</h2>
      <div className="mb-8 grid grid-cols-[repeat(auto-fill,minmax(250px,1fr))] gap-6">
        {CARTE_ESEMPIO.map((carta) => (
          <CartaGioco key={carta.codice} carta={carta} />
        ))}
      </div>

      <h2 className="mb-3 font-semibold">Medie (come nella battaglia) e piccole (come negli scarti)</h2>
      <div className="flex flex-wrap items-end gap-4">
        {CARTE_ESEMPIO.map((carta) => (
          <div key={carta.codice} className="flex items-end gap-2">
            <CartaGioco carta={carta} className="w-31" />
            <CartaGioco carta={carta} className="w-17.5" />
          </div>
        ))}
      </div>
    </main>
  )
}
