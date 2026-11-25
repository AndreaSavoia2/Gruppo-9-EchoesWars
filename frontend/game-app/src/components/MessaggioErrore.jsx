/**
 * Messaggio da mostrare quando una fetch non va a buon fine.
 *
 * @param {object} props - le proprietà del componente
 * @param {string} [props.testo='Impossibile caricare i dati. Riprova più tardi.']
 * @returns {JSX.Element}
 */
export default function MessaggioErrore({ testo = 'Impossibile caricare i dati. Riprova più tardi.' }) {
  return (
    <p role="alert" className="text-didascalia font-medium text-errore">
      {testo}
    </p>
  )
}
