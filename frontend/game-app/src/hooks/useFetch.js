import { useCallback, useEffect, useState } from 'react'

/**
 * Reusable hook: calls a fetch function and gives you 4 things:
 *   data    → the backend response (null until it arrives)
 *   loading → true while we wait for the FIRST response
 *   error   → the error, if something goes wrong
 *   reload  → function to repeat the request (e.g. after a change)
 *
 * Usage: const { data, loading, error, reload } = useFetch(getDashboard)
 *
 * @param {(signal: AbortSignal) => Promise<any>} fetcher - must be stable (useCallback or a module function)
 * @returns {{ data: any, loading: boolean, error: Error|null, reload: Function }}
 */
export function useFetch(fetcher) {
  const [status, setStatus] = useState({ data: null, loading: true, error: null })
  // Every time "version" changes, the request is made again
  const [version, setVersion] = useState(0)

  useEffect(() => {
    const controller = new AbortController()

    fetcher(controller.signal)
      .then((data) => setStatus({ data, loading: false, error: null }))
      .catch((error) => {
        // AbortError = request cancelled by us: it is not a real error
        if (error.name !== 'AbortError') {
          setStatus({ data: null, loading: false, error })
        }
      })

    // If the component disappears, cancel the request
    return () => controller.abort()
  }, [fetcher, version])

  const reload = useCallback(() => setVersion((v) => v + 1), [])

  return { ...status, reload }
}
