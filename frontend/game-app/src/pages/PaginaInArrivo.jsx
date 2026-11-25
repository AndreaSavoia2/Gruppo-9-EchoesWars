import { Link } from 'react-router'
import AppLayout from '../components/AppLayout'
import Intestazione from '../components/Intestazione'

// Pagina temporanea per i link verso schermate non ancora fatte.
// Quando creiamo la pagina vera, aggiungiamo la sua <Route> in App.jsx.
export default function PaginaInArrivo() {
  return (
    <AppLayout>
      <Intestazione titolo="In arrivo" sottotitolo="Questa pagina non è ancora stata creata.">
        <Link to="/" className="font-medium underline">
          ‹ Torna alla Home
        </Link>
      </Intestazione>
    </AppLayout>
  )
}
