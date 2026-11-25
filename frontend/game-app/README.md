## Flavio

# Echoes' War · Piattaforma di gioco (game-app)

La parte "loggata" di Echoes' War: Home, Gioca (partita casuale e sfida un amico),
battaglia animata, Collezione, Mazzi, Buste, Scambi, Amici, Richieste, Negozio, Profilo,
Prove degli Echi, Cronologia, Primo accesso e schermate di errore.

**Tecnologie:** React 19 + Vite + Tailwind CSS v4 + React Router.

## Come avviarla

```bash
npm install
```

Con il backend vero (Docker del backend acceso, vedi `backend/README.md`):

```bash
npm run dev
```

Senza backend, con dati finti (modalità demo):

```bash
npm run demo
```

Poi apri l'indirizzo che compare nel terminale (di solito `http://localhost:5173`).

## Configurazione (`.env`)

Copia `.env.example`, rinominalo `.env` e compila:

| Variabile | A cosa serve | Esempio |
| --- | --- | --- |
| `VITE_API_URL` | indirizzo del backend (finisce con `/api/v1`) | `http://localhost:8080/api/v1` |
| `VITE_LOGIN_URL` | pagina di login statica (sito di Alessia) | `http://localhost:5500/accesso.html` |

Il file `.env` **non** va caricato su GitHub (è già nel `.gitignore`).

Nel `.env` del **backend** serve anche permettere l'app React:
`CORS_ALLOWED_ORIGINS=http://localhost:5173,http://localhost:5500`

## Collegamento con il backend

Cosa è collegato, cosa manca e cosa va corretto nel backend:
[`docs/stato-api.md`](docs/stato-api.md).

Le chiamate sono nei file `src/api/*.js`. Il backend risponde
`{ timestamp, httpStatus, message, data }`: `src/api/client.js` restituisce alle pagine solo `data`,
e `src/api/backend.js` traduce i dati (es. le carte) nel formato usato dalle pagine.

## Login e token JWT

Il login è nelle pagine statiche (`frontend/website`). Dopo il login, la pagina statica
porta l'utente all'app con il token nell'indirizzo (`#token=...`).
Il token dura 15 minuti: l'app lo rinnova da sola con il cookie di rinnovo del backend.
Tutto il funzionamento è spiegato in [`docs/login-e-token.md`](docs/login-e-token.md).

## Modalità demo

`npm run demo` usa un backend finto (`src/api/demo.js`) per provare il flusso
partita casuale senza backend: matchmaking → battaglia animata → risultato → dettagli.
Con la demo attiva c'è anche la vetrina delle carte su `/carte-demo`.

## Carte

Le carte sono composte a strati: **illustrazione + cornice** (due immagini dal backend)
e sopra i **testi in HTML** (costo, nome, scuola, eco, effetto, ATK, DEF).
Così i valori possono cambiare in battaglia e gli screen reader leggono la carta.
Componente: `src/components/carta/CartaGioco.jsx`. Zone delle cornici: `src/data/carte.js`.

## Struttura

```
src/
  api/          chiamate al backend (un file per sezione) + token + demo
  components/   componenti riutilizzabili (bottoni, barre, carte, modali...)
  context/      dati dell'utente condivisi in tutta l'app
  data/         dati fissi (menu, regole, rarità, cornici delle carte)
  hooks/        hook riutilizzabili (useFetch, useOnline...)
  pages/        una pagina per ogni schermata
  utils/        funzioni di supporto (date, iniziali...)
public/
  icons/, images/   icone e immagini (le cornici in images/cornici servono solo come riserva)
docs/
  login-e-token.md  come la pagina di login statica passa il token all'app
  stato-api.md      cosa è collegato al backend e cosa manca
```
