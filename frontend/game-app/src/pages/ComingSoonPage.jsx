import { Link } from 'react-router'
import AppLayout from '../components/AppLayout'
import PageHeader from '../components/PageHeader'

/**
 * Temporary page for the links to screens that are not done yet.
 * When we create the real page, we add its <Route> in App.jsx.
 *
 * @returns {JSX.Element}
 */
export default function ComingSoonPage() {
  return (
    <AppLayout>
      <PageHeader title="In arrivo" subtitle="Questa pagina non è ancora stata creata.">
        <Link to="/" className="font-medium underline">
          ‹ Torna alla Home
        </Link>
      </PageHeader>
    </AppLayout>
  )
}
