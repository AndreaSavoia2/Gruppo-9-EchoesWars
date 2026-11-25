import { useCallback, useEffect, useState } from 'react'

// Ti dice se il dispositivo è connesso a internet (true/false).
// Il browser lancia due eventi: "offline" quando la rete cade, "online" quando torna.
//
// Uso: const { online, ricontrolla } = useOnline()
//   ricontrolla() → rilegge subito lo stato della rete (per il bottone "Riprova")
export function useOnline() {
  const [online, setOnline] = useState(navigator.onLine)

  useEffect(() => {
    const vaOnline = () => setOnline(true)
    const vaOffline = () => setOnline(false)
    window.addEventListener('online', vaOnline)
    window.addEventListener('offline', vaOffline)
    // Quando il componente sparisce, smetti di ascoltare
    return () => {
      window.removeEventListener('online', vaOnline)
      window.removeEventListener('offline', vaOffline)
    }
  }, [])

  const ricontrolla = useCallback(() => setOnline(navigator.onLine), [])

  return { online, ricontrolla }
}
