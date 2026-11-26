import GameCard from '../components/card/GameCard'
import { SAMPLE_CARDS } from '../api/demo'

/**
 * 🧪 Only in demo mode: /cards-demo
 * Shows the sample cards at 3 different sizes, to check
 * that frame, illustration and texts stay in their place.
 *
 * @returns {JSX.Element}
 */
export default function CardShowcase() {
  return (
    <main className="min-h-screen bg-background px-5 py-8 lg:px-12">
      <h1 className="font-title text-h2">Vetrina carte (demo)</h1>
      <p className="mb-6">Cornice + illustrazione + testi. Le carte senza illustrazione mostrano il segnaposto.</p>

      <h2 className="mb-3 font-semibold">Grandi</h2>
      <div className="mb-8 grid grid-cols-[repeat(auto-fill,minmax(250px,1fr))] gap-6">
        {SAMPLE_CARDS.map((card) => (
          <GameCard key={card.code} card={card} />
        ))}
      </div>

      <h2 className="mb-3 font-semibold">Medie (come nella battaglia) e piccole (come negli scarti)</h2>
      <div className="flex flex-wrap items-end gap-4">
        {SAMPLE_CARDS.map((card) => (
          <div key={card.code} className="flex items-end gap-2">
            <GameCard card={card} className="w-31" />
            <GameCard card={card} className="w-17.5" />
          </div>
        ))}
      </div>
    </main>
  )
}
