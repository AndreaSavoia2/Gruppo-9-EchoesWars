import Schede from '../Schede'

const schede = [
  { id: 'carte', etichetta: 'Carte', percorso: '/collezione' },
  { id: 'mazzi', etichetta: 'Mazzi', percorso: '/collezione/mazzi' },
  { id: 'buste', etichetta: 'Buste', percorso: '/collezione/buste' },
]

/**
 * Le 3 schede in alto: Carte · Mazzi · Buste. "attiva" = quella selezionata.
 *
 * @param {object} props - le proprietà del componente
 * @param {*} props.attiva
 * @returns {JSX.Element}
 */
export default function SchedeCollezione({ attiva }) {
  return <Schede schede={schede} attiva={attiva} etichetta="Sezioni della collezione" />
}
