import { lazy, Suspense } from "react";
import { BrowserRouter, Routes, Route } from "react-router";
import { UtenteProvider } from "./context/UtenteContext";
import Dashboard from "./pages/Dashboard";
import ComeVuoiGiocare from "./pages/ComeVuoiGiocare";
import ScegliMazzoCasuale from "./pages/ScegliMazzoCasuale";
import Partita from "./pages/Partita";
import DettagliPartita from "./pages/DettagliPartita";
import Replay from "./pages/Replay";
import ScegliAmico from "./pages/ScegliAmico";
import ScegliMazzoSfida from "./pages/ScegliMazzoSfida";
import AttesaSfida from "./pages/AttesaSfida";
import Collezione from "./pages/Collezione";
import DettaglioCarta from "./pages/DettaglioCarta";
import Buste from "./pages/Buste";
import Mazzi from "./pages/Mazzi";
import NuovoMazzo from "./pages/NuovoMazzo";
import EditorMazzo from "./pages/EditorMazzo";
import Scambi from "./pages/Scambi";
import NuovaProposta from "./pages/NuovaProposta";
import ConfermaScambio from "./pages/ConfermaScambio";
import Amici from "./pages/Amici";
import Richieste from "./pages/Richieste";
import AccettaSfida from "./pages/AccettaSfida";
import Negozio from "./pages/Negozio";
import Profilo from "./pages/Profilo";
import ModificaAvatar from "./pages/ModificaAvatar";
import CambiaPassword from "./pages/CambiaPassword";
import Lingua from "./pages/Lingua";
import ProveEchi from "./pages/ProveEchi";
import Cronologia from "./pages/Cronologia";
import Accessi from "./pages/Accessi";
import PrimoAccesso from "./pages/PrimoAccesso";
import ControlloApp from "./components/ControlloApp";
// 🧪 Vetrina delle carte: esiste solo in modalità demo (lazy = caricata a parte, non finisce nell'app vera)
const VetrinaCarte = import.meta.env.VITE_DEMO === 'true' ? lazy(() => import('./pages/VetrinaCarte')) : null
import PaginaInArrivo from "./pages/PaginaInArrivo";

/**
 * L'app intera: router con tutte le pagine, dati dell'utente e controlli (rete, server, primo accesso).
 *
 * @returns {JSX.Element}
 */
export default function App() {
  return (
    // BrowserRouter: legge l'indirizzo nella barra del browser
    <BrowserRouter>
      {/* UtenteProvider scarica i dati dell'utente UNA volta per tutta l'app */}
      <UtenteProvider>
        {/* ControlloApp: prima di ogni pagina controlla rete, server e primo accesso */}
        <ControlloApp>
          {/* Routes: "se l'indirizzo è X, mostra la pagina Y" */}
          <Routes>
            <Route path="/" element={<Dashboard />} />
            <Route path="/gioca" element={<ComeVuoiGiocare />} />
            <Route path="/gioca/casuale" element={<ScegliMazzoCasuale />} />
            <Route path="/gioca/amico" element={<ScegliAmico />} />
            <Route path="/sfida/:id/mazzo" element={<ScegliMazzoSfida />} />
            <Route path="/sfida/:id" element={<AttesaSfida />} />
            {/* ":id" = pezzo variabile: /partita/42, /partita/43... */}
            <Route path="/partita/:id" element={<Partita />} />
            <Route path="/partita/:id/dettagli" element={<DettagliPartita />} />
            <Route path="/partita/:id/battaglia" element={<Replay modo="battaglia" />} />
            <Route path="/partita/:id/replay" element={<Replay modo="replay" />} />
            <Route path="/collezione" element={<Collezione />} />
            <Route path="/collezione/carta/:id" element={<DettaglioCarta />} />
            <Route path="/collezione/buste" element={<Buste />} />
            <Route path="/collezione/mazzi" element={<Mazzi />} />
            <Route path="/collezione/mazzi/nuovo" element={<NuovoMazzo />} />
            <Route
              path="/collezione/mazzi/nuovo/carte"
              element={<EditorMazzo />}
            />
            <Route path="/collezione/mazzi/:id" element={<EditorMazzo />} />
            <Route path="/scambi" element={<Scambi />} />
            <Route path="/scambi/nuova" element={<NuovaProposta />} />
            <Route path="/scambi/:id" element={<ConfermaScambio />} />
            <Route path="/amici" element={<Amici />} />
            <Route path="/richieste" element={<Richieste />} />
            <Route path="/richieste/:id/mazzo" element={<AccettaSfida />} />
            <Route path="/negozio" element={<Negozio />} />
            <Route path="/profilo" element={<Profilo />} />
            <Route path="/profilo/avatar" element={<ModificaAvatar />} />
            <Route path="/profilo/password" element={<CambiaPassword />} />
            <Route path="/profilo/lingua" element={<Lingua />} />
            <Route path="/missioni" element={<ProveEchi />} />
            <Route path="/cronologia" element={<Cronologia />} />
            <Route path="/accessi" element={<Accessi />} />
            <Route path="/benvenuto" element={<PrimoAccesso />} />
            {VetrinaCarte && (
            <Route
              path="/carte-demo"
              element={
                <Suspense>
                  <VetrinaCarte />
                </Suspense>
              }
            />
          )}
          {/* "*" = qualsiasi altro indirizzo */}
            <Route path="*" element={<PaginaInArrivo />} />
          </Routes>
        </ControlloApp>
      </UtenteProvider>
    </BrowserRouter>
  );
}
