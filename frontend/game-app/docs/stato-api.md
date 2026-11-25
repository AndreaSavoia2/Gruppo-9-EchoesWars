# Stato del collegamento con il backend

Cosa usa la piattaforma di gioco (`frontend/game-app`) del backend **Management-Service**
(`backend` branch). Tutti i percorsi partono da `/api/v1`.

Le chiamate sono nei file `src/api/*.js`: ogni funzione dice se è **✅ COLLEGATA**
o **⚠️ NON ANCORA NEL BACKEND**.

## ✅ Collegato

| Pagina | Endpoint |
| --- | --- |
| Barra in alto (nome, crediti) | `GET /auth/me`, `GET /private/credits/balance` |
| Rinnovo automatico del token | `GET /auth/csrf`, `POST /auth/refresh` |
| Esci dall'account | `POST /auth/logout` |
| Collezione, dettaglio carta | `GET /private/collections` (tutte le pagine) |
| Mazzi, Scegli il mazzo | `GET /private/decks?status=READY`, `GET /private/decks/{id}` |
| Crea mazzo | `POST /private/decks` |
| Modifica mazzo | `PUT /private/decks/{id}` ⚠️ bloccato dal CORS (vedi sotto) |
| Negozio | `GET /private/packs`, `POST /private/packs/{id}/purchases` |
| Buste | `GET /private/packs/purchases/unopened`, `GET .../legendary-guarantee-progress`, `POST .../open` |
| Amici: aggiungi | `POST /private/friendships/requests/{username}` |
| Richieste di amicizia ricevute | `GET /private/friendships/requests/received` |
| Modifica avatar | `PUT /auth/me/selected-avatar` ⚠️ bloccato dal CORS (vedi sotto) |

## 🔧 Da correggere nel backend

1. **CORS: mancano `PUT` e `DELETE`.** `CorsConfig.ALLOWED_METHODS` contiene solo `GET` e `POST`:
   dal browser non si possono modificare/eliminare mazzi né scegliere l'avatar.
   Serve: `GET, POST, PUT, DELETE, OPTIONS`.
2. **CORS: origini.** Nel `.env` del backend:
   `CORS_ALLOWED_ORIGINS=http://localhost:5173,http://localhost:5500` (app React + sito statico).

## ➕ Dati che mancano nelle risposte

| Dove | Cosa manca | Cosa fa ora il frontend |
| --- | --- | --- |
| `CardResponse` | codice dell'**effetto** (es. `PIERCING`) | mostra la descrizione; filtro "Abilità" vuoto |
| `GamePackageResponse` | **prezzo** del pacchetto | usa 300 (il valore nel database) |
| `/auth/me` | immagine dell'**avatar** (c'è solo l'id), **primo accesso** | icona standard; il benvenuto non parte da solo |
| `GET /private/decks` | carte e punti di ogni mazzo | chiede il dettaglio di ogni mazzo (1 chiamata per mazzo) |
| `GET /private/collections` | filtri (scuola, eco, rarità) | scarica tutte le pagine e filtra nel browser |
| `tiers.image_url` | immagini delle **cornici** (vuote nei dati iniziali) | usa le cornici di prova in `public/images/cornici` |

## ⏳ Endpoint che non esistono ancora

Queste pagine restano sui percorsi provvisori (rispondono con errore finché il backend non li aggiunge):

- **Partite**: avvio partita casuale, stato, risultato, battaglia/replay, cronologia
- **Sfide** a un amico (invia, accetta, rifiuta, stato)
- **Amici**: lista amici, accetta/rifiuta richiesta, elimina amico
- **Scambi**
- **Dashboard** (home), **Prove degli Echi** (missioni), **Accessi consecutivi**
- **Carta**: dettaglio per id, conversione dei doppioni, copie usate nei mazzi
- **Avatar**: lista degli avatar disponibili
- **Profilo**: partite giocate, vittorie, numero di carte

Per provare queste parti senza backend: `npm run demo`.
