import { lazy, Suspense } from "react";
import { BrowserRouter, Routes, Route } from "react-router";
import { UserProvider } from "./context/UserContext";
import Dashboard from "./pages/Dashboard";
import HowToPlay from "./pages/HowToPlay";
import ChooseRandomDeck from "./pages/ChooseRandomDeck";
import Match from "./pages/Match";
import MatchDetails from "./pages/MatchDetails";
import Replay from "./pages/Replay";
import ChooseFriend from "./pages/ChooseFriend";
import ChooseChallengeDeck from "./pages/ChooseChallengeDeck";
import ChallengeWaiting from "./pages/ChallengeWaiting";
import Collection from "./pages/Collection";
import CardDetail from "./pages/CardDetail";
import Packs from "./pages/Packs";
import Decks from "./pages/Decks";
import NewDeck from "./pages/NewDeck";
import DeckEditor from "./pages/DeckEditor";
import Trades from "./pages/Trades";
import NewProposal from "./pages/NewProposal";
import ConfirmTrade from "./pages/ConfirmTrade";
import Friends from "./pages/Friends";
import Requests from "./pages/Requests";
import AcceptChallenge from "./pages/AcceptChallenge";
import Shop from "./pages/Shop";
import Profile from "./pages/Profile";
import EditAvatar from "./pages/EditAvatar";
import ChangePassword from "./pages/ChangePassword";
import Language from "./pages/Language";
import EchoTrials from "./pages/EchoTrials";
import MatchHistory from "./pages/MatchHistory";
import DailyLogins from "./pages/DailyLogins";
import FirstLogin from "./pages/FirstLogin";
import AppGuard from "./components/AppGuard";
// 🧪 Card showcase: only exists in demo mode (lazy = loaded separately, it never ends up in the real app)
const CardShowcase = import.meta.env.VITE_DEMO === 'true' ? lazy(() => import('./pages/CardShowcase')) : null
import ComingSoonPage from "./pages/ComingSoonPage";

/**
 * The whole app: router with every page, user data and checks (network, server, first login).
 *
 * @returns {JSX.Element}
 */
export default function App() {
  return (
    // BrowserRouter: reads the address in the browser bar
    <BrowserRouter>
      {/* UserProvider downloads the user data ONCE for the whole app */}
      <UserProvider>
        {/* AppGuard: before every page it checks network, server and first login */}
        <AppGuard>
          {/* Routes: "if the address is X, show page Y" */}
          <Routes>
            <Route path="/" element={<Dashboard />} />
            <Route path="/play" element={<HowToPlay />} />
            <Route path="/play/random" element={<ChooseRandomDeck />} />
            <Route path="/play/friend" element={<ChooseFriend />} />
            <Route path="/challenge/:id/deck" element={<ChooseChallengeDeck />} />
            <Route path="/challenge/:id" element={<ChallengeWaiting />} />
            {/* ":id" = variable part: /match/42, /match/43... */}
            <Route path="/match/:id" element={<Match />} />
            <Route path="/match/:id/details" element={<MatchDetails />} />
            <Route path="/match/:id/battle" element={<Replay way="battle" />} />
            <Route path="/match/:id/replay" element={<Replay way="replay" />} />
            <Route path="/collection" element={<Collection />} />
            <Route path="/collection/card/:id" element={<CardDetail />} />
            <Route path="/collection/packs" element={<Packs />} />
            <Route path="/collection/decks" element={<Decks />} />
            <Route path="/collection/decks/new" element={<NewDeck />} />
            <Route
              path="/collection/decks/new/cards"
              element={<DeckEditor />}
            />
            <Route path="/collection/decks/:id" element={<DeckEditor />} />
            <Route path="/trades" element={<Trades />} />
            <Route path="/trades/new" element={<NewProposal />} />
            <Route path="/trades/:id" element={<ConfirmTrade />} />
            <Route path="/friends" element={<Friends />} />
            <Route path="/requests" element={<Requests />} />
            <Route path="/requests/:id/deck" element={<AcceptChallenge />} />
            <Route path="/shop" element={<Shop />} />
            <Route path="/profile" element={<Profile />} />
            <Route path="/profile/avatar" element={<EditAvatar />} />
            <Route path="/profile/password" element={<ChangePassword />} />
            <Route path="/profile/language" element={<Language />} />
            <Route path="/missions" element={<EchoTrials />} />
            <Route path="/history" element={<MatchHistory />} />
            <Route path="/logins" element={<DailyLogins />} />
            <Route path="/welcome" element={<FirstLogin />} />
            {CardShowcase && (
            <Route
              path="/cards-demo"
              element={
                <Suspense>
                  <CardShowcase />
                </Suspense>
              }
            />
          )}
          {/* "*" = any other address */}
            <Route path="*" element={<ComingSoonPage />} />
          </Routes>
        </AppGuard>
      </UserProvider>
    </BrowserRouter>
  );
}
