import { useCallback, useEffect, useState } from 'react'

/**
 * Tells you whether the device is connected to the internet (true/false).
 * The browser fires two events: "offline" when the network drops, "online" when it comes back.
 *
 * Usage: const { online, recheck } = useOnline()
 *   recheck() → reads the network status again straight away (for the "Riprova" button)
 * @returns {{ online: boolean, recheck: Function }}
 */
export function useOnline() {
  const [online, setOnline] = useState(navigator.onLine)

  useEffect(() => {
    const goOnline = () => setOnline(true)
    const goOffline = () => setOnline(false)
    window.addEventListener('online', goOnline)
    window.addEventListener('offline', goOffline)
    // When the component disappears, stop listening
    return () => {
      window.removeEventListener('online', goOnline)
      window.removeEventListener('offline', goOffline)
    }
  }, [])

  const recheck = useCallback(() => setOnline(navigator.onLine), [])

  return { online, recheck }
}
