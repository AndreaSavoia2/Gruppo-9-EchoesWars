import { StrictMode } from 'react'
import { createRoot } from 'react-dom/client'
import './index.css'
import App from './App.jsx'
import { prendiTokenDallIndirizzo, serveLogin, vaiAlLogin } from './api/token'

// 🔑 Prima di tutto: se arriviamo dal login statico, salva il token (#token=...)
prendiTokenDallIndirizzo()

// Senza token (e con un login configurato in .env) → subito alla pagina di login.
// In modalità demo (npm run demo) il login non serve.
const demo = import.meta.env.VITE_DEMO === 'true'
if (!demo && serveLogin()) {
  vaiAlLogin()
} else {

  createRoot(document.getElementById('root')).render(
    <StrictMode>
      <App />
    </StrictMode>,
  )
}
