/**
 * Message to show when a fetch does not succeed.
 *
 * @param {object} props - the component props
 * @param {string} [props.text='Impossibile caricare i dati. Riprova più tardi.']
 * @returns {JSX.Element}
 */
export default function ErrorMessage({ text = 'Impossibile caricare i dati. Riprova più tardi.' }) {
  return (
    <p role="alert" className="text-caption font-medium text-error">
      {text}
    </p>
  )
}
