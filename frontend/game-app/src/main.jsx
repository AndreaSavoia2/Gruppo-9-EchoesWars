import { StrictMode } from 'react'
import { createRoot } from 'react-dom/client'
import './index.css'
import App from './App.jsx'
import { takeTokenFromUrl, needsLogin, goToLogin } from './api/token'
import { applyLanguage } from './utils/language'

// 🌍 The language chosen by the user goes into the lang attribute of the page
applyLanguage()

// 🔑 First of all: if we come from the static login, save the token (#token=...)
takeTokenFromUrl()

// Without a token (and with a login configured in .env) → straight to the login page.
// In demo mode (npm run demo) no login is needed.
const demo = import.meta.env.VITE_DEMO === 'true'
if (!demo && needsLogin()) {
  goToLogin()
} else {

  createRoot(document.getElementById('root')).render(
    <StrictMode>
      <App />
    </StrictMode>,
  )
}
