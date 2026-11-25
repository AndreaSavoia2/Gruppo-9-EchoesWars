// Titolo (Cinzel) + sottotitolo in cima alle schermate dell'app.
// children: cose extra sotto il sottotitolo (es. il link "‹ Home" su desktop)
export default function Intestazione({ titolo, sottotitolo, children }) {
  return (
    <div className="flex flex-col items-start gap-1 lg:gap-1.5">
      <h1 className="font-titolo text-h2">{titolo}</h1>
      {sottotitolo && <p>{sottotitolo}</p>}
      {children}
    </div>
  )
}
