import Tabs from '../Tabs'

const tabs = [
  { id: 'cards', label: 'Carte', path: '/collection' },
  { id: 'decks', label: 'Mazzi', path: '/collection/decks' },
  { id: 'packs', label: 'Buste', path: '/collection/packs' },
]

/**
 * The 3 tabs at the top: Carte · Mazzi · Buste. "active" = the selected one.
 *
 * @param {object} props - the component props
 * @param {'cards'|'decks'|'packs'} props.active
 * @returns {JSX.Element}
 */
export default function CollectionTabs({ active }) {
  return <Tabs tabs={tabs} active={active} label="Sezioni della collezione" />
}
