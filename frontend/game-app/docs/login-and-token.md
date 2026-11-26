# Login (static pages) ↔ App (React): how to pass the token

The login lives in the **static pages**. The game app is in **React**.
This document is the agreement between the two parts on how to pass the **JWT token**.

> The token is **not created by any page**: it is created by the **Spring backend** in `POST /api/v1/auth/login`.
> The pages receive it and pass it to the React app.

---

## The flow in 5 steps

```
STATIC PAGE (accesso.html)                    SPRING BACKEND              REACT APP
1. the user types email and password
2. POST /api/v1/auth/login               ──►  checks and creates the token
   { email, password }                         (+ refresh cookie, 7 days)
3. receives { "accessToken": "eyJ..." }  ◄──
4. brings the user to the app with the token:
   http://localhost:5173/#token=eyJ...   ─────────────────────────────►  5. saves the token,
                                                                            removes it from the address
                                                                            and uses it for every request
```

**Why in the address, after `#`?** The static page and the React app can run on different addresses
(in development for example `localhost:5500` and `localhost:5173`), so they do not share `localStorage`.
The part after `#` **is never sent to any server** and the React app deletes it as soon as it has read it.

---

## What the static login page must do

The texts shown to the user stay in Italian; the `motivo` parameter and its values
(`scaduto`, `uscito`, `password`) are part of the agreement, so they must not change.

```html
<form id="login-form">
  <label>Email <input type="email" name="email" required autocomplete="email"></label>
  <label>Password <input type="password" name="password" required autocomplete="current-password"></label>
  <p id="error" role="alert" hidden></p>
  <button type="submit">Accedi</button>
</form>

<script>
  // ⚙️ To be changed when the website goes online
  const BACKEND = 'http://localhost:8080/api/v1/auth'   // login address in the Spring backend
  const APP = 'http://localhost:5173/'                  // address of the React app

  // Message if the app sent us back here (?motivo=scaduto, ?motivo=uscito or ?motivo=password)
  const reason = new URLSearchParams(location.search).get('motivo')
  const messages = {
    scaduto: 'La sessione è scaduta: accedi di nuovo.',
    uscito: 'Sei uscito dall’account.',
    password: 'Password cambiata: accedi con la nuova password.',
  }
  if (messages[reason]) showMessage(messages[reason])

  document.getElementById('login-form').addEventListener('submit', async (event) => {
    event.preventDefault() // do not reload the page
    const data = new FormData(event.target)

    try {
      const response = await fetch(`${BACKEND}/login`, {
        // ⭐ IMPORTANT: without this line the browser does not save the refresh cookie
        // and after 15 minutes the user would have to log in again
        credentials: 'include',
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ email: data.get('email'), password: data.get('password') }),
      })

      if (!response.ok) {
        showMessage('Email o password sbagliate.')
        return
      }

      // The backend answers { timestamp, httpStatus, message, data: { accessToken, tokenType, expiresAt } }
      const { data: body } = await response.json()
      const token = body.accessToken
      // ⭐ The important step: go to the app with the token after the #
      window.location.href = `${APP}#token=${encodeURIComponent(token)}`
    } catch {
      showMessage('Server non raggiungibile. Riprova tra qualche minuto.')
    }
  })

  function showMessage(text) {
    const error = document.getElementById('error')
    error.textContent = text
    error.hidden = false
  }
</script>
```

### Registration
The registration is `POST /api/v1/auth/register` with `{ "username": "...", "email": "...", "password": "..." }`.
After a successful registration (response `201`) there are two options:
- **simplest**: bring the user to the login page (`accesso.html`), so they log in with the data just created;
- **handiest**: immediately call `POST /api/v1/auth/login` too and then go to the app with `#token=...`, as above.

---

## What the React app already does (no need to touch it)

| Situation | What happens |
|---|---|
| It arrives with `#token=...` | saves the token (`localStorage`, key `echoeswar_token`) and removes it from the address |
| The app is opened without a token | goes to the static login page |
| The backend answers **401** and the token cannot be refreshed | deletes the token and goes to the login with `?motivo=scaduto` |
| The user presses **Esci** (log out) in the Profile | deletes the token and goes to the login with `?motivo=uscito` |
| The user **changes the password** | the backend closes the sessions: the app goes to the login with `?motivo=password` |
| Every request to the backend | adds the header `Authorization: Bearer <token>` |

In the React app the address of the login page is set in the `.env` file:
```
VITE_LOGIN_URL=http://localhost:5500/accesso.html
```

---

## What is needed from the Spring backend
- ✅ already done: `POST /api/v1/auth/login` → `{ "data": { "accessToken": "eyJ...", ... } }` + refresh cookie
- ✅ already done: the token lasts 15 minutes, the React app refreshes it by itself (`/api/v1/auth/csrf` + `/api/v1/auth/refresh`)
- ✅ already done: `POST /api/v1/auth/register` → response `201`
- CORS: in the backend `.env` `CORS_ALLOWED_ORIGINS=http://localhost:5173,http://localhost:5500` (React app + static page)
- All the other endpoints: answer `401` if the token is missing or not valid

## ⚠️ Security rules
- **Never** save the password: neither in `localStorage` nor in the address. The token yes, the password no.
- The token in the address must be **only** after `#`, never after `?`: what is after `?` reaches the servers.
- Online, both the website and the app must use **https**.
