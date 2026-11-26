# Status of the connection with the backend

What the game platform (`frontend/game-app`) uses from the **Management-Service** backend
(`backend` branch). All the paths start with `/api/v1`.

The calls are in the `src/api/*.js` files: every function says whether it is **✅ CONNECTED**
or **⚠️ NOT IN THE BACKEND YET**.

## ✅ Connected

| Page | Endpoint |
| --- | --- |
| Top bar (name, credits) | `GET /auth/me`, `GET /private/credits/balance` |
| Automatic token refresh | `GET /auth/csrf`, `POST /auth/refresh` |
| Log out | `POST /auth/logout` |
| Change password | `POST /auth/change-password` |
| Collection, card detail | `GET /private/collections` (all the pages) |
| Decks, Choose the deck | `GET /private/decks?status=READY`, `GET /private/decks/{id}` |
| Create deck | `POST /private/decks` |
| Edit deck | `PUT /private/decks/{id}` ⚠️ blocked by CORS (see below) |
| Shop | `GET /private/packs`, `POST /private/packs/{id}/purchases` |
| Packs | `GET /private/packs/purchases/unopened`, `GET .../legendary-guarantee-progress`, `POST .../open` |
| Friends: add | `POST /private/friendships/requests/{username}` |
| Received friend requests | `GET /private/friendships/requests/received` |
| Edit avatar | `PUT /auth/me/selected-avatar` ⚠️ blocked by CORS (see below) |

## 🔧 To be fixed in the backend

1. **CORS: `PUT` and `DELETE` are missing.** `CorsConfig.ALLOWED_METHODS` only contains `GET` and `POST`:
   from the browser it is not possible to edit/delete decks or choose the avatar.
   Needed: `GET, POST, PUT, DELETE, OPTIONS`.
2. **CORS: origins.** In the backend `.env`:
   `CORS_ALLOWED_ORIGINS=http://localhost:5173,http://localhost:5500` (React app + static website).

## ➕ Data missing in the responses

| Where | What is missing | What the frontend does now |
| --- | --- | --- |
| `CardResponse` | **effect** code (e.g. `PIERCING`) | shows the description; "Abilità" filter empty |
| `CardResponse` | `artUrl` often empty | uses `public/images/illustrations/{code}.webp` |
| `GamePackageResponse` | pack **price** | uses 300 (the value in the database) |
| `/auth/me` | **avatar** image (only the id is there), **first login** | default icon; the welcome does not start by itself |
| `GET /private/decks` | cards and points of every deck | asks for the detail of every deck (1 call per deck) |
| `GET /private/collections` | filters (school, echo, rarity) | downloads all the pages and filters in the browser |
| `tiers.image_url` | **frame** images (empty in the seed data) | uses the sample frames in `public/images/frames` |

## ⏳ Endpoints that do not exist yet

These pages use provisional paths (they answer with an error until the backend adds them).
The expected request/response shapes are written in the JSDoc of each function.

| Feature | Provisional paths (file) |
| --- | --- |
| **Matches**: start, status, result, battle/replay, history | `POST /matches`, `GET /matches/{id}`, `GET /matches/{id}/replay`, `GET /matches?outcome=` (`matches.js`, `progress.js`) |
| **Challenges** to a friend | `GET /friends`, `POST /challenges`, `GET /challenges/{id}`, `POST /challenges/{id}/deck`, `POST /challenges/{id}/cancel` (`challenges.js`) |
| **Friends**: list, remove | `GET /friends`, `DELETE /friends/{id}` (`friends.js`) |
| **Requests**: accept / decline | `POST /requests/{id}/accept`, `POST /requests/{id}/decline` (`requests.js`) |
| **Trades** | `GET /trades`, `GET /trades/{id}`, `GET /trades/friends`, `GET /trades/my-cards`, `POST /trades`, `POST /trades/{id}/accept`… (`trades.js`) |
| **Dashboard** (home) | `GET /dashboard` (`dashboard.js`, with a fallback on the existing endpoints) |
| **Echo Trials**, **Consecutive logins** | `GET /trials`, `POST /trials/{id}/claim`, `GET /logins` (`progress.js`) |
| **Card**: duplicate conversion | `POST /collection/cards/{id}/convert` (`collection.js`) |
| **Avatars**: available list | `GET /profile/avatar` (`profile.js`) |
| **Welcome** seen | `POST /user/welcome` (`user.js`) |

To try these parts without the backend: `npm run demo`.
