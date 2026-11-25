# Login (pagine statiche) ↔ App (React): come passare il token

Il login è nelle **pagine statiche**. L'app di gioco è in **React**.
Questo documento è l'accordo tra le due parti su come passare il **token JWT**.

> Il token **non lo crea nessuna pagina**: lo crea il **backend Spring** in `POST /api/login`.
> Le pagine lo ricevono e lo passano all'app React.

---

## Il giro in 5 passi

```
PAGINA STATICA (accesso.html)                 BACKEND SPRING              APP REACT
1. l'utente scrive email e password
2. POST /api/login { email, password }  ──►  controlla e crea il token
3. riceve { "token": "eyJ..." }          ◄──
4. porta l'utente all'app con il token:
   http://localhost:5173/#token=eyJ...   ─────────────────────────────►  5. salva il token,
                                                                            lo toglie dall'indirizzo
                                                                            e lo usa per tutte le richieste
```

**Perché nell'indirizzo, dopo `#`?** La pagina statica e l'app React possono girare su indirizzi diversi
(in sviluppo per esempio `localhost:5500` e `localhost:5173`), quindi non condividono il `localStorage`.
La parte dopo `#` **non viene mai mandata a nessun server** e l'app React la cancella appena l'ha letta.

---

## Cosa deve fare la pagina statica di login

```html
<form id="form-accesso">
  <label>Email <input type="email" name="email" required autocomplete="email"></label>
  <label>Password <input type="password" name="password" required autocomplete="current-password"></label>
  <p id="errore" role="alert" hidden></p>
  <button type="submit">Accedi</button>
</form>

<script>
  // ⚙️ Da cambiare quando il sito va online
  const BACKEND = 'http://localhost:8080/api'   // indirizzo del backend Spring
  const APP = 'http://localhost:5173/'          // indirizzo dell'app React

  // Messaggio se l'app ci ha rimandato qui (?motivo=scaduto o ?motivo=uscito)
  const motivo = new URLSearchParams(location.search).get('motivo')
  const messaggi = {
    scaduto: 'La sessione è scaduta: accedi di nuovo.',
    uscito: 'Sei uscito dall’account.',
  }
  if (messaggi[motivo]) mostraMessaggio(messaggi[motivo])

  document.getElementById('form-accesso').addEventListener('submit', async (evento) => {
    evento.preventDefault() // non ricaricare la pagina
    const dati = new FormData(evento.target)

    try {
      const risposta = await fetch(`${BACKEND}/login`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ email: dati.get('email'), password: dati.get('password') }),
      })

      if (!risposta.ok) {
        mostraMessaggio('Email o password sbagliate.')
        return
      }

      const { token } = await risposta.json()
      // ⭐ Il passaggio importante: vai all'app con il token dopo il #
      window.location.href = `${APP}#token=${encodeURIComponent(token)}`
    } catch {
      mostraMessaggio('Server non raggiungibile. Riprova tra qualche minuto.')
    }
  })

  function mostraMessaggio(testo) {
    const errore = document.getElementById('errore')
    errore.textContent = testo
    errore.hidden = false
  }
</script>
```

### Registrazione
Dopo `POST /api/registrazione` andata a buon fine (risposta `201`), le strade sono due:
- **più semplice**: porta l'utente alla pagina di login (`accesso.html`), così accede con i dati appena creati;
- **più comoda**: fai subito anche `POST /api/login` e poi vai all'app con `#token=...`, come sopra.

---

## Cosa fa già l'app React (non serve toccarla)

| Situazione | Cosa succede |
|---|---|
| Arriva con `#token=...` | salva il token (`localStorage`, chiave `echoeswar_token`) e lo toglie dall'indirizzo |
| Si apre l'app senza token | porta alla pagina di login statica |
| Il backend risponde **401** (token scaduto o falso) | cancella il token e porta al login con `?motivo=scaduto` |
| L'utente preme **Esci** nel Profilo | cancella il token e porta al login con `?motivo=uscito` |
| Ogni richiesta al backend | aggiunge l'header `Authorization: Bearer <token>` |

Nell'app React l'indirizzo della pagina di login si imposta nel file `.env`:
```
VITE_LOGIN_URL=http://localhost:5500/accesso.html
```

---

## Cosa serve dal backend Spring
- `POST /api/login` → risposta `{ "token": "eyJ..." }`, oppure `401` se i dati sono sbagliati
- `POST /api/registrazione` → risposta `201`
- CORS: permettere **sia** l'indirizzo della pagina statica (es. `http://localhost:5500`) **sia** quello dell'app React (`http://localhost:5173`)
- Tutti gli altri endpoint: rispondere `401` se manca il token o non è valido

## ⚠️ Regole di sicurezza
- **Mai** salvare la password: né nel `localStorage` né nell'indirizzo. Il token sì, la password no.
- Il token nell'indirizzo deve stare **solo** dopo `#`, mai dopo `?`: quello che sta dopo `?` arriva ai server.
- Online, sia il sito sia l'app devono usare **https**.
