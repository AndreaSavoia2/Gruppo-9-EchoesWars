import { createContext, useContext, useState } from 'react'
import { useFetch } from '../hooks/useFetch'
import { getUser } from '../api/user'

// The "context" is a shared box: the user data
// is downloaded ONCE and any component can read it
// with useUser(), without passing it by hand from component to component.
// It also contains "reload": useful when the credits change (e.g. after a match).
// welcomeSeen / markWelcomeSeen: they remember that the user has just finished
// (or skipped) the welcome, so they do not go back to it while the backend updates.
const UserContext = createContext(null)

/**
 * Downloads the user data only once and makes it available to the whole app.
 *
 * @param {object} props - the component props
 * @param {React.ReactNode} props.children
 * @returns {JSX.Element}
 */
export function UserProvider({ children }) {
  const userState = useFetch(getUser)
  const [welcomeSeen, setWelcomeSeen] = useState(false)
  const value = { ...userState, welcomeSeen, markWelcomeSeen: () => setWelcomeSeen(true) }
  return <UserContext.Provider value={value}>{children}</UserContext.Provider>
}

/**
 * Reads the shared user data (data, loading, error, reload...).
 * @returns {object}
 */
export function useUser() {
  return useContext(UserContext)
}
