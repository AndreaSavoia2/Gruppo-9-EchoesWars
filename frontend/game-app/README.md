## Flavio

# Echoes' War · Game platform (game-app)

The "logged-in" part of Echoes' War: Home, Play (random match and friend challenge),
animated battle, Collection, Decks, Packs, Trades, Friends, Requests, Shop, Profile,
Echo Trials, Match history, First login and error screens.

**Tech stack:** React 19 + Vite + Tailwind CSS v4 + React Router.

> **Language:** the code, the comments and the docs are in English.
> The texts shown to the user are in Italian (the game is for Italian players).

## How to start it

```bash
npm install
```

With the real backend (backend Docker running, see `backend/README.md`):

```bash
npm run dev
```

Without the backend, with fake data (demo mode):

```bash
npm run demo
```

Then open the address shown in the terminal (usually `http://localhost:5173`).

## Configuration (`.env`)

Copy `.env.example`, rename it `.env` and fill it in:

| Variable | What it is for | Example |
| --- | --- | --- |
| `VITE_API_URL` | backend address (ends with `/api/v1`) | `http://localhost:8080/api/v1` |
| `VITE_LOGIN_URL` | static login page (Alessia's website) | `http://localhost:5500/accesso.html` |

The `.env` file must **not** be pushed to GitHub (it is already in `.gitignore`).

The **backend** `.env` must also allow the React app:
`CORS_ALLOWED_ORIGINS=http://localhost:5173,http://localhost:5500`

## Connection to the backend

What is connected, what is missing and what must be fixed in the backend:
[`docs/api-status.md`](docs/api-status.md).

The calls are in the `src/api/*.js` files. The backend answers
`{ timestamp, httpStatus, message, data }`: `src/api/client.js` gives the pages only `data`,
and `src/api/backend.js` translates the data (e.g. the cards) into the format used by the pages.

## Login and JWT token

The login lives in the static pages (`frontend/website`). After the login, the static page
brings the user to the app with the token in the address (`#token=...`).
The token lasts 15 minutes: the app refreshes it by itself with the backend refresh cookie.
Everything is explained in [`docs/login-and-token.md`](docs/login-and-token.md).

## Demo mode

`npm run demo` uses a fake backend (`src/api/demo.js`) to try the random match
flow without the backend: matchmaking → animated battle → result → details.
In demo mode there is also a card showcase at `/cards-demo`.

## Cards

Cards are built in layers: **illustration + frame** (two images from the backend)
and on top the **HTML texts** (cost, name, school, echo, effect, ATK, DEF).
This way the values can change during the battle and screen readers can read the card.
Component: `src/components/card/GameCard.jsx`. Frame zones: `src/data/cards.js`.

The 48 card illustrations are also in `public/images/illustrations`, named after the
card code (e.g. `DOM01.webp`): they are used when the backend sends no `artUrl`.

## Animations

The pack opening and the battle animations are written in plain CSS:
`src/components/packs/pack-opening.css` (classes `opening-…`) and
`src/components/replay/battle.css` (classes `battle-…`).
Both respect the system "reduce motion" setting.

## Structure

```
src/
  api/          backend calls (one file per section) + token + demo
  components/   reusable components (buttons, bars, cards, modals...)
  context/      user data shared across the whole app
  data/         fixed data (menu, rules, rarities, card frames)
  hooks/        reusable hooks (useFetch, useOnline, useCountDown...)
  pages/        one page per screen
  utils/        helper functions (dates, initials, language...)
public/
  icons/, images/   icons and images (images/frames is only a fallback,
                    images/illustrations holds the card illustrations)
docs/
  login-and-token.md  how the static login page passes the token to the app
  api-status.md       what is connected to the backend and what is missing
  card-images.md      list of the card image files (illustrations and frames)
```
