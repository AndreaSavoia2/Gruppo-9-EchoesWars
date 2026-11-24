# Regole GitHub – Frontend EchoesWars

Nov 24, 2026 · @Alessia

## 1. Dove lavora ognuno

Tutto il codice frontend sta nella cartella `frontend/`, accanto alla cartella `backend/` del team backend. Ognuno lavora **solo nella propria cartella**.

| Cartella | Chi | Cosa contiene | Tecnologie |
| --- | --- | --- | --- |
| `frontend/game-app/` | Flavio | Piattaforma di gioco (parte loggata) | React + Tailwind |
| `frontend/website/` | Alessia | Pagine statiche del sito | HTML + JS + Tailwind |
| `frontend/backoffice/` | Leonardo | Gestione catalogo carte e pacchetti | HTML + JS + Tailwind |
| `frontend/shared/` | Tutti (con accordo) | Colori, font, logo, immagini delle carte | CSS |

- Se devi modificare `shared/`, avvisa prima gli altri: quei file li usano tutti.
- Non toccare mai la cartella `backend/`.

## 2. Le parole da conoscere

- **Repository (repo):** la cartella del progetto su GitHub, con tutta la sua storia.
- **Branch:** una copia parallela del progetto. Ci lavori senza rompere il lavoro degli altri.
- **Commit:** un salvataggio delle tue modifiche, con una frase che dice cosa hai fatto.
- **Push:** carichi i tuoi commit dal tuo computer su GitHub.
- **Pull:** scarichi da GitHub le modifiche fatte dagli altri.
- **Pull Request (PR):** la richiesta di unire il tuo branch a un altro. Un compagno controlla le modifiche prima di accettarle.
- **Merge:** l'unione vera e propria, che avviene quando la PR viene accettata.
- **Conflitto:** succede quando due persone modificano le stesse righe dello stesso file. Va risolto a mano scegliendo quale versione tenere.
- **Issue:** un compito da fare, scritto su GitHub Projects (es. "Pagina FAQ"). Ha un numero, es. #12.

## 3. I branch

Il frontend ha un branch comune, **`frontend`**, che raccoglie il lavoro di tutti. Nessuno ci scrive direttamente: ci si arriva solo con una Pull Request.

| Branch | A cosa serve | Chi ci lavora |
| --- | --- | --- |
| `main` | Versione finale e stabile del progetto | Nessuno direttamente |
| `frontend` | Unisce il lavoro di tutto il frontend | Nessuno direttamente, solo PR |
| `fe-game/nome-feature` | Una funzione della piattaforma di gioco | Flavio |
| `fe-site/nome-feature` | Una pagina o funzione del sito statico | Alessia |
| `fe-backoffice/nome-feature` | Una schermata del backoffice | Leonardo |

- **Un branch per ogni funzione**, non uno per persona. Esempi: `fe-game/collezione`, `fe-site/faq`, `fe-backoffice/lista-carte`.
- **Nomi in minuscolo**, parole separate da trattini, senza spazi né accenti.
- **Usa sempre il prefisso `fe-`.** Nomi come `feature/backoffice` sono già usati dal backend.

## 4. Il flusso di lavoro, passo per passo

Ogni nuova funzione segue sempre questi passi. Esempio: la pagina FAQ.

1. **Prendi l'issue.** Su GitHub Projects apri la card "Pagina FAQ" (es. #12), spostala in *In progress*.
2. **Aggiorna `frontend`.** Spostati sul branch `frontend` e fai *pull*, così parti dall'ultima versione.
3. **Crea il tuo branch** partendo da `frontend`: `fe-site/faq`.
4. **Lavora e fai commit spesso**, uno per ogni piccola modifica completata.
5. **Fai push** del branch su GitHub, anche più volte al giorno. Così il lavoro è salvato anche online.
6. **Apri la Pull Request** da `fe-site/faq` verso `frontend` (vedi sezione 6).
7. **Aspetta la review.** Se ti chiedono correzioni, falle sullo stesso branch e fai di nuovo push: la PR si aggiorna da sola.
8. **Merge e pulizia.** Dopo l'approvazione si fa il merge, si cancella il branch e la card passa in *Done*.

Quando `frontend` è stabile e testato, verrà aperta una PR da `frontend` verso `main`.

## 5. I commit

I commit si scrivono **in italiano**, come fa anche il backend.

- **Un commit = una modifica.** Non mettere insieme cose diverse (es. pagina FAQ + fix del menù).
- **Messaggio breve e chiaro** (massimo una riga), che dica cosa hai fatto.
- **Usa un prefisso** per dire il tipo di modifica:

| Prefisso | Quando si usa | Esempio |
| --- | --- | --- |
| `feat:` | Nuova funzione o pagina | `feat: aggiunta pagina FAQ` |
| `fix:` | Correzione di un errore | `fix: corretto menù su mobile` |
| `style:` | Solo grafica, nessuna logica | `style: allineate le card della collezione` |
| `docs:` | README o JSDoc | `docs: aggiunto JSDoc alle funzioni api` |
| `refactor:` | Codice riscritto meglio, stesso risultato | `refactor: separato componente Card` |

- **Da evitare:** messaggi come "modifiche", "fix", "aaa", "prova".

## 6. Le Pull Request

Una PR serve a far controllare il tuo lavoro prima che entri in `frontend`. Nessun codice entra senza che almeno un'altra persona l'abbia visto.

**Come aprirla su GitHub**

1. Dopo il push, GitHub mostra il pulsante giallo **Compare & pull request**. Cliccalo.
2. Controlla le due tendine in alto: **base** = `frontend`, **compare** = il tuo branch. Attenzione a non scegliere `main` o `backend`.
3. **Titolo:** come un commit, es. `feat: pagina FAQ`.
4. **Descrizione:** cosa hai fatto, come l'hai provato, uno screenshot se è una parte grafica.
5. Scrivi **`Closes #12`** (il numero dell'issue): quando la PR viene unita, l'issue si chiude da sola.
6. A destra, in **Reviewers**, scegli Alessia.

**Regole della review**

- Non si approva la propria PR: la controlla sempre un'altra persona.
- Chi fa la review guarda le modifiche nella scheda *Files changed* e lascia commenti sulle righe.
- Le correzioni si fanno sullo stesso branch: la PR si aggiorna da sola.
- Il merge lo fa chi ha aperto la PR, solo dopo l'approvazione.
- **PR piccole:** meglio una PR per pagina che una enorme con tutto dentro.

## 7. Cosa non fare e checklist

**Da non fare mai**

- Lavorare o fare push direttamente su `main`, `frontend` o sui branch del backend.
- Caricare `node_modules/`, `dist/` o file con password e chiavi (es. `.env`).
- Modificare file fuori dalla tua cartella senza avvisare.
- Lasciare un branch fermo per giorni: più aspetti, più conflitti ci saranno.
- Andare nel panico per un conflitto: chiedi aiuto prima di cancellare qualcosa.

**Checklist prima di aprire una PR**

- [ ] Ho fatto pull di `frontend` e il mio branch è aggiornato.
- [ ] Ho provato la pagina su desktop e su mobile.
- [ ] Non ci sono errori nella console del browser.
- [ ] Le funzioni JS hanno il commento JSDoc.
- [ ] I commit sono in italiano e con il prefisso giusto.
- [ ] La PR va verso `frontend` e contiene `Closes #numero-issue`.
