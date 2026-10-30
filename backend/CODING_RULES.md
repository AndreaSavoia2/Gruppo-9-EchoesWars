# EchoesWar — Convenzioni di programmazione backend

> **Documento ufficiale per gli sviluppatori backend**  
> Lingua della documentazione: italiano  
> Ambito: stile del codice, nomenclatura, persistenza, API, testabilità e collaborazione.

## Indice

1. [Principi e livelli delle regole](#1-principi-e-livelli-delle-regole)
2. [Convenzioni di denominazione](#2-convenzioni-di-denominazione)
3. [Versioni e dipendenze](#3-versioni-e-dipendenze)
4. [Organizzazione dei package](#4-organizzazione-dei-package)
5. [Stile Java e tipizzazione](#5-stile-java-e-tipizzazione)
6. [Lombok e costruttori](#6-lombok-e-costruttori)
7. [Entity e convenzioni database](#7-entity-e-convenzioni-database)
8. [Repository e query](#8-repository-e-query)
9. [DTO, mapping e validazione](#9-dto-mapping-e-validazione)
10. [Service e transazioni](#10-service-e-transazioni)
11. [Convenzioni REST](#11-convenzioni-rest)
12. [Configurazione, commenti e documentazione](#12-configurazione-commenti-e-documentazione)
13. [Git e controllo delle modifiche](#13-git-e-controllo-delle-modifiche)
14. [Checklist Pull Request](#14-checklist-pull-request)

## 1. Principi e livelli delle regole

- **MUST**: obbligatorio; le violazioni devono essere corrette prima del merge.
- **SHOULD**: raccomandato; eventuali eccezioni richiedono una motivazione.
- **MAY**: facoltativo, purché coerente con le altre convenzioni.

Le regole si applicano al nuovo codice e alle parti modificate. Il codice deve essere leggibile, coerente e privo di complessità non necessaria.

## 2. Convenzioni di denominazione

**MUST:** identificatori, commenti, messaggi applicativi, JavaDoc, descrizioni OpenAPI e commit in **inglese**. Il testo esplicativo di questo documento resta in italiano.

| Elemento | Convenzione | Esempio |
| --- | --- | --- |
| Group ID | `it.echoeswar` | `it.echoeswar` |
| Artifact ID | minuscolo, separato da trattini | `echoeswar-gestionale` |
| Package | minuscolo, nomi significativi per funzionalità | `it.echoeswar.gestionale.deck` |
| Classi e interfacce | `PascalCase` | `DeckService` |
| Entity | singolare, senza suffisso `Entity` | `Player`, `Card` |
| Repository | nome entità + `Repository` | `PlayerRepository` |
| Controller | nome risorsa + `Controller` | `DeckController` |
| Service | nome responsabilità + `Service` | `DeckService` |
| Implementazione con interfaccia | suffisso `Impl` | `DeckServiceImpl` |
| DTO | nome risorsa + `Request`/`Response` | `DeckRequest`, `DeckResponse` |
| Mapper | nome risorsa + `Mapper` | `DeckMapper` |
| Eccezioni | nome problema + `Exception` | `DeckNotFoundException` |
| Metodi e variabili | `camelCase`, nomi descrittivi | `calculateDamage`, `currentRound` |
| Boolean | `is`, `has`, `can` quando appropriato | `isActive`, `hasWinner` |
| Costanti | `UPPER_SNAKE_CASE` | `MAX_ROUNDS` |
| Enum | tipo `PascalCase`, valori `UPPER_SNAKE_CASE` | `MatchStatus.IN_PROGRESS` |
| Collection | nomi plurali descrittivi | `cards`, `activePlayers` |
| SQL e JPQL | keyword in maiuscolo | `SELECT`, `WHERE`, `JOIN` |

**SHOULD:** evitare abbreviazioni ambigue (`usr`, `tmp`, `mgr`) e nomi generici (`Helper`, `Manager`, `Data`), salvo casi motivati. Le sigle tecniche comuni (`HTTP`, `DTO`, `JWT`) sono consentite.

## 3. Versioni e dipendenze

| Voce | Regola |
| --- | --- |
| Linguaggio | Java 25 (**MUST**) |
| Framework applicativo | Spring Boot 4.1.x, con versione esatta condivisa (**MUST**) |
| Database | PostgreSQL 18 |
| Build | Maven 3.9.x |
| Gestione versioni | Parent POM / BOM centralizzato (**MUST**) |
| Migration SQL | Flyway (**MUST**) |
| Lombok | Consentito dove previsto nel capitolo 6; non nel codice Java puro cui si applica tale vincolo |
| Nuove dipendenze | Aggiunta tramite Pull Request approvata (**MUST**) |

**MUST:** non utilizzare versioni dinamiche (`LATEST`, `RELEASE`, intervalli non controllati) e non aggiungere dipendenze duplicate o inutilizzate.  
**SHOULD:** utilizzare Maven Wrapper e verificare compatibilità e manutenzione delle librerie prima di introdurle.

Le dipendenze di sicurezza saranno definite separatamente; non sono ancora normate qui.

## 4. Organizzazione dei package

I package devono rispettare le convenzioni di denominazione del capitolo 2. Le classi vengono collocate nella funzionalità cui appartengono; all'interno delle funzionalità si utilizzano, quando necessari, i layer convenzionali.

```text
it.echoeswar.gestionale.deck
├── controller
├── service
├── repository
├── entity
├── dto
│   ├── request
│   └── response
├── mapper
└── exception
```

- **MUST:** separare i DTO di richiesta da quelli di risposta.
- **MUST:** collocare i mapper nella funzionalità di riferimento.
- **MUST:** mantenere le eccezioni specifiche vicino alla funzionalità; quelle condivise possono essere in `exception` comune.
- **SHOULD:** creare soltanto package effettivamente necessari.
- **SHOULD:** riservare `util` a funzionalità generiche e realmente riutilizzabili, senza nascondervi logica di dominio.

## 5. Stile Java e tipizzazione

- **MUST:** dichiarare esplicitamente i tipi; `var` è vietato.
- **MUST:** usare `Optional<T>` **solo come tipo di ritorno nei Repository**. Non impiegarlo nei DTO, come campo o parametro, né come ritorno dei Service.
- **MUST:** non restituire `null` dai metodi applicativi, salvo eccezioni tecniche documentate.
- **MUST:** restituire collection vuote anziché `null`.
- **SHOULD:** usare primitivi per valori sempre presenti e wrapper (`Integer`, `Long`, `BigDecimal`) quando l'assenza ha significato.
- **SHOULD:** preferire l'immutabilità per input, risultati e oggetti di valore.
- **MAY:** utilizzare Stream API per filtri e trasformazioni semplici; preferire cicli espliciti per logiche complesse o con effetti collaterali.
- **MAY:** utilizzare `record`, switch expressions e pattern matching quando migliorano la leggibilità.

```java
Optional<Player> player = playerRepository.findById(playerId);

Player existingPlayer = player.orElseThrow(
        () -> new PlayerNotFoundException(playerId)
);

List<Card> cards = List.of(); // Never return null for an empty collection.
```

## 6. Lombok e costruttori

| Annotazione o pratica | Regola |
| --- | --- |
| `@Getter` | MAY |
| `@Setter` | Solo quando la mutabilità è necessaria (**SHOULD**) |
| `@Data` | Vietata (**MUST**) |
| `@Builder` | Preferita per classi DTO e oggetti con molti campi (**SHOULD**) |
| `@NoArgsConstructor`, `@AllArgsConstructor` | Consentite quando servono (**MAY**) |
| `@RequiredArgsConstructor` | Preferita per constructor injection (**SHOULD**) |
| `@Slf4j` | Preferita quando serve logging (**SHOULD**) |
| `@EqualsAndHashCode`, `@ToString` su Entity | Generazione automatica vietata (**MUST**) |
| Dependency injection | Constructor injection obbligatoria; field injection vietata (**MUST**) |

Sulle Entity utilizzare solo annotazioni Lombok mirate, in particolare `@Getter`, `@Setter` selettivi e `@NoArgsConstructor`. Per i costruttori JPA senza argomenti preferire la visibilità `protected`.

```java
@Service
@RequiredArgsConstructor
public class DeckService {
    private final DeckRepository deckRepository;
}
```

## 7. Entity e convenzioni database

| Elemento | Convenzione |
| --- | --- |
| Tabelle | plurale, `snake_case`: `players`, `match_results` (**MUST**) |
| Colonne | `snake_case`: `created_at`, `player_id` (**MUST**) |
| Primary key | `BIGINT` auto-incrementale in PostgreSQL, mappata come `Long` in Java (**MUST**) |
| Mapping JPA | `@Table`, `@Column`, `@JoinColumn` espliciti (**MUST**) |
| Relazioni | unidirezionali di default, bidirezionali se motivate (**SHOULD**) |
| Fetch | preferire `LAZY`; caricamento mirato quando necessario (**SHOULD**) |
| Molti-a-molti | preferire Entity associative esplicite (**SHOULD**) |
| Vincoli | `NOT NULL`, `UNIQUE`, `FOREIGN KEY`, `CHECK` anche nel DB (**MUST**) |
| Audit | `created_at`, `updated_at` quando pertinenti (**SHOULD**) |
| Concorrenza | `@Version` e/o lock transazionali nei casi critici (**MUST**) |
| Documenti strutturati | `JSONB` per replay/eventi, colonne normali per campi interrogati spesso |

**MUST:** per le chiavi primarie auto-incrementali utilizzare `Long` nelle Entity, nei Repository, nei DTO e nei parametri delle API. Usare `@GeneratedValue(strategy = GenerationType.IDENTITY)` e lasciare al database la generazione degli identificativi; non assegnare manualmente gli ID.

Esempio SQL PostgreSQL per una nuova tabella:

```sql
CREATE TABLE decks (
    id BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    name VARCHAR(100) NOT NULL
);
```

```java
@Entity
@Table(name = "decks")
@Getter
@NoArgsConstructor
public class Deck {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "name", nullable = false)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "player_id", nullable = false)
    private Player player;

    @Version
    @Column(name = "version", nullable = false)
    private Long version;
}
```

### Migrazioni e sincronizzazione schema

- **MUST:** ogni modifica condivisa allo schema deve avere una nuova migration Flyway; non modificare migration già condivise/applicate.
- **MAY:** in un database locale *sperimentale e sacrificabile* si può usare `ddl-auto: update` con Flyway disabilitato.
- **MUST:** nei database condivisi, di test, di demo e di produzione utilizzare Flyway e `ddl-auto: validate`.
- **MUST:** prima del merge, tradurre in migration Flyway le modifiche sperimentate localmente tramite `update`.

## 8. Repository e query

- **MUST:** i Repository estendono `JpaRepository<Entity, Long>` per le Entity con identificativo numerico auto-incrementale.
- **SHOULD:** preferire nell'ordine metodi derivati, JPQL, SQL nativo.
- **MUST:** usare SQL nativo soltanto con motivazione tecnica esplicita.
- **MUST:** nelle query esplicite usare alias, parametri nominati e parole chiave maiuscole.
- **MUST:** per elenchi potenzialmente grandi usare `Pageable` e una risposta paginata.
- **MUST:** restituire Entity (o collection di Entity) per le letture di dominio; sono ammessi risultati scalari per `exists`, `count` e analoghi.
- **MUST:** non usare DTO projection o `@EntityGraph`; per relazioni richieste si può usare `JOIN FETCH` in JPQL.
- **SHOULD:** preferire operazioni JPA standard a query bulk; usare `@Modifying` per casi motivati.
- **MUST:** non accedere ai Repository direttamente dai Controller.

```java
public interface CardRepository extends JpaRepository<Card, Long> {

    Optional<Card> findByName(String name);

    Page<Card> findByRarity(CardRarity rarity, Pageable pageable);

    @Query("SELECT c FROM Card c WHERE c.attack >= :minimumAttack")
    List<Card> findByMinimumAttack(@Param("minimumAttack") int minimumAttack);
}
```

Le transazioni applicative vengono gestite principalmente nei Service. Evitare query che causino N+1 o carichino dati non necessari.

## 9. DTO, mapping e validazione

- **SHOULD:** preferire `record` per i DTO; classi consentite se motivate.
- **MUST:** definire tipi distinti `Request` e `Response`.
- **MUST:** non esporre Entity JPA nelle API, nemmeno nelle API interne.
- **MUST:** applicare Bean Validation ai Request DTO quando pertinente.
- **MUST:** tenere la validazione business nei Service o nei componenti di dominio, distinta dalla validazione strutturale.
- **MUST:** utilizzare mapper manuali dedicati; i mapper non eseguono query né logica business.
- **MUST:** utilizzare un `ErrorResponse` personalizzato, includendo l'elenco dei campi non validi nelle risposte di validazione.
- **MUST:** usare ISO 8601 e UTC per gli istanti temporali nelle API.
- **MUST:** includere esplicitamente le proprietà `null` nelle risposte JSON.
- **MUST:** mantenere versionato e documentato il contratto JSON tra sistemi, con DTO definiti separatamente.

```java
public record DeckRequest(
        @NotBlank String name,
        @NotNull List<@NotNull Long> cardIds
) {}

public record FieldErrorResponse(String field, String message) {}

public record ErrorResponse(
        Instant timestamp,
        int status,
        String errorCode,
        String message,
        String path,
        List<FieldErrorResponse> errors
) {}
```

## 10. Service e transazioni

- **SHOULD:** creare un'interfaccia Service soltanto quando offre un vantaggio reale; altrimenti usare una classe concreta.
- **MUST:** collocare la logica applicativa nei Service o in componenti di dominio dedicati, mai nei Controller.
- **MUST:** definire `@Transactional` sui metodi Service che costituiscono un'unità di lavoro.
- **SHOULD:** utilizzare `@Transactional(readOnly = true)` quando serve un contesto transazionale di lettura.
- **MUST:** rispettare il rollback standard per `RuntimeException`; configurare `rollbackFor` per checked exception quando necessario.
- **MUST:** selezionare lock ottimistici/pessimistici secondo il caso d'uso e verificarli con test concorrenti.
- **MUST:** non eseguire chiamate HTTP esterne mentre è aperta una transazione database.
- **MUST:** utilizzare eccezioni di business specifiche; evitare `catch` generici che nascondono errori.
- **MUST:** delegare la conversione Entity/DTO ai mapper.
- **SHOULD:** limitare la visibilità pubblica alle operazioni necessarie; i metodi di supporto devono essere privati quando possibile.
- **MUST:** inviare notifiche conseguenti a modifiche persistenti soltanto dopo il commit; per necessità di consegna garantita usare un meccanismo affidabile.

**Nota tecnica:** i metodi transazionali devono essere invocati in modo che l'intercettazione di Spring sia effettiva; una chiamata interna alla stessa istanza non attiva il proxy transazionale standard.

## 11. Convenzioni REST

| Aspetto | Regola |
| --- | --- |
| API pubbliche | prefisso `/api/v1` (**MUST**) |
| API interne | prefisso `/internal/v1` (**MUST**) |
| URL | plurali, inglese, minuscolo, `kebab-case` nei nomi composti (**MUST**) |
| ID risorsa | `@PathVariable` (**MUST**) |
| Filtri, paginazione, ordinamento | `@RequestParam` (**MUST**) |
| Body | DTO request con `@RequestBody` e `@Valid` quando applicabile |
| Risposta Controller | sempre `ResponseEntity` (**MUST**) |
| Letture paginate | `page`, `size`, `sort`; dimensione massima e campi ordinabili validati (**MUST**) |
| Modifica totale | `PUT` (**MUST**) |
| Modifica parziale | `PATCH` (**MUST**) |
| Eliminazione completata | `204 No Content` (**MUST**) |
| Lavoro asincrono accettato | `202 Accepted` con identificativo e stato (**MUST**) |
| Operazioni critiche ripetibili | `Idempotency-Key` quando applicabile (**MUST**) |
| Documentazione | OpenAPI/Swagger per tutti gli endpoint pubblici (**MUST**) |

Codici HTTP principali: `200`, `201`, `202`, `204`, `400`, `401`, `403`, `404`, `405`, `409`, `422`, `500`. Non utilizzare `200` per mascherare un errore applicativo.

```java
@Operation(summary = "Retrieve a card by ID")
@GetMapping("/{id}")
public ResponseEntity<CardResponse> findById(@PathVariable Long id) {
    return ResponseEntity.ok(cardService.findById(id));
}
```

Per un'operazione che genera lavoro asincrono, restituire `202 Accepted` con un DTO che contenga l'ID della risorsa o operazione e il suo stato iniziale; quando pertinente aggiungere l'header `Location`.

Le chiavi di idempotenza devono essere verificate in modo persistente e sicuro rispetto alle richieste concorrenti, associandole all'operazione e al soggetto che la richiede.

## 12. Configurazione, commenti e documentazione

### File di configurazione

- **MUST:** preferire `application.yml` ai file `.properties`.
- **MUST:** usare indentazione YAML coerente (spazi, non tab).
- **MUST:** non inserire credenziali, chiavi o token nel repository.
- **SHOULD:** separare per ambiente le configurazioni non sensibili.

### Commenti e JavaDoc

- **MUST:** commenti e JavaDoc in inglese.
- **SHOULD:** commentare il *perché* di una decisione non ovvia, evitando di ripetere il comportamento evidente del codice.
- **SHOULD:** documentare i metodi pubblici con contratti non banali e le eccezioni rilevanti.
- **MAY:** utilizzare `@param`, `@return`, `@throws` secondo necessità.

```java
/**
 * Calculates the damage inflicted by an attack.
 *
 * @param attack the attack value
 * @param defense the defense value
 * @return the non-negative damage amount
 */
public int calculateDamage(int attack, int defense) {
    return Math.max(0, attack - defense);
}
```

### Documentazione Swagger / OpenAPI

La documentazione OpenAPI descrive il contratto HTTP delle API REST e viene resa consultabile tramite Swagger UI. Ogni endpoint pubblico deve essere documentato e la documentazione deve essere aggiornata nella stessa Pull Request che modifica l'endpoint.

#### Regole di scrittura

| Elemento | Convenzione | Livello |
| --- | --- | --- |
| Lingua | `summary`, `description`, nomi e descrizioni degli schemi in inglese | **MUST** |
| Controller | `@Tag` per raggruppare gli endpoint della stessa risorsa | **SHOULD** |
| Endpoint | `@Operation` con un riepilogo breve e una descrizione dell'azione | **MUST** |
| Parametri | `@Parameter` per i parametri non autoesplicativi, con descrizione ed esempio coerenti | **SHOULD** |
| Risposte | `@ApiResponses` / `@ApiResponse` per i principali esiti HTTP reali | **MUST** |
| Contenuto | `@Content` e `@Schema` per documentare i corpi di risposta, quando presenti | **MUST** |
| DTO | `@Schema` sui campi che necessitano di esempi, vincoli o spiegazioni | **SHOULD** |
| Errori | Usare lo stesso `ErrorResponse` definito nel capitolo 9 | **MUST** |
| Coerenza | Non documentare risposte o campi che l'endpoint non restituisce | **MUST** |
| API interne | Documentarle quando utili all'integrazione, mantenendo separata la loro esposizione pubblica | **SHOULD** |

Le annotazioni OpenAPI **non sostituiscono** `@Valid`, Bean Validation, `@RequestMapping`, `@PathVariable` o le verifiche effettuate dai Service. La documentazione deve riflettere il comportamento del codice, non definirlo implicitamente.

#### Annotazioni principali

- `@Tag`: nome e descrizione del gruppo di operazioni, normalmente sul Controller.
- `@Operation`: `summary` e `description` dell'endpoint.
- `@Parameter`: descrizione di un parametro di percorso o query; il campo `required` deve essere coerente con il contratto reale.
- `@ApiResponse`: codice HTTP, descrizione e corpo della risposta.
- `@ApiResponses`: raccoglie più `@ApiResponse`.
- `@Content`: specifica media type e schema del corpo; per risposte senza body può essere usato `@Content` vuoto.
- `@Schema`: specifica tipo, descrizione, esempio e altri metadati di DTO e proprietà.

#### Esempio completo: endpoint GET per identificativo

L'esempio usa identificativi `Long` auto-incrementali e DTO `record`, secondo le convenzioni di questo documento. Gli import Java sono omessi per brevità.

```java
@RestController
@RequestMapping("/api/v1/cards")
@RequiredArgsConstructor
@Tag(name = "Cards", description = "Operations for browsing cards")
public class CardController {

    private final CardService cardService;

    @Operation(
            summary = "Retrieve a card by ID",
            description = "Returns the details of the card identified by its numeric ID."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Card retrieved successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = CardResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid card ID format",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Card not found",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)
                    )
            )
    })
    @GetMapping("/{id}")
    public ResponseEntity<CardResponse> findById(
            @Parameter(
                    description = "Numeric identifier of the card",
                    example = "42",
                    required = true
            )
            @PathVariable Long id) {

        CardResponse response = cardService.findById(id);
        return ResponseEntity.ok(response);
    }
}
```

I codici `400` e `404` sono esempi validi se il progetto mappa effettivamente tali errori sul formato `ErrorResponse` condiviso.

#### Esempio di DTO documentato

Usare `@Schema` nei componenti del record per mostrare descrizioni ed esempi direttamente nello schema OpenAPI.

```java
@Schema(description = "Card details returned by the API")
public record CardResponse(
        @Schema(description = "Generated card ID", example = "42")
        Long id,

        @Schema(description = "Card name", example = "Iron Guardian")
        String name,

        @Schema(description = "Base attack value", example = "5")
        int attack,

        @Schema(description = "Base defense value", example = "3")
        int defense
) {}
```

#### Esempio: creazione di una risorsa

Le operazioni di creazione devono documentare anche il Request DTO e il codice `201 Created` quando la risorsa viene effettivamente creata.

```java
@Operation(
        summary = "Create a deck",
        description = "Creates a new deck after validating the submitted data."
)
@ApiResponses({
        @ApiResponse(
                responseCode = "201",
                description = "Deck created successfully",
                content = @Content(
                        mediaType = "application/json",
                        schema = @Schema(implementation = DeckResponse.class)
                )
        ),
        @ApiResponse(
                responseCode = "400",
                description = "Invalid request data",
                content = @Content(
                        mediaType = "application/json",
                        schema = @Schema(implementation = ErrorResponse.class)
                )
        ),
        @ApiResponse(
                responseCode = "409",
                description = "Deck creation conflicts with existing data",
                content = @Content(
                        mediaType = "application/json",
                        schema = @Schema(implementation = ErrorResponse.class)
                )
        )
})
@PostMapping
public ResponseEntity<DeckResponse> create(
        @Valid @RequestBody DeckRequest request) {

    DeckResponse response = deckService.create(request);
    return ResponseEntity.status(HttpStatus.CREATED).body(response);
}
```

Se l'endpoint non può effettivamente restituire `409`, tale risposta deve essere rimossa dalla sua documentazione. Per un'operazione asincrona, documentare invece `202 Accepted` e il DTO contenente ID e stato iniziale.

#### Verifica della documentazione

Prima della Pull Request:

1. Avviare l'applicazione con la dipendenza OpenAPI compatibile con la versione di Spring Boot adottata.
2. Verificare che gli endpoint compaiano nella specifica OpenAPI generata, normalmente disponibile su `/v3/api-docs`.
3. Aprire Swagger UI, normalmente disponibile su `/swagger-ui/index.html`, se abilitata.
4. Controllare nomi dei parametri, tipi dei DTO, esempi, codici HTTP e corpi d'errore.
5. Verificare che la documentazione sia coerente con i test degli endpoint e non esponga informazioni riservate.

I percorsi riportati sono quelli tipici delle integrazioni springdoc; possono variare in base alla configurazione. **MUST:** non pubblicare Swagger UI o la specifica delle API interne in ambienti esposti senza una decisione esplicita sulle modalità di accesso.

## 13. Git e controllo delle modifiche

- **MUST:** utilizzare messaggi di commit in italiano, chiari e descrittivi.
- **SHOULD:** effettuare commit piccoli, frequenti e relativi a un'unica modifica logica.
- **SHOULD:** usare prefissi esplicativi quando utili: `feat:`, `fix:`, `test:`, `docs:`, `refactor:`.
- **MUST:** sottoporre le nuove dipendenze all'approvazione tramite Pull Request.
- **MUST:** non includere nel repository file contenenti segreti o artefatti locali non necessari.
- **SHOULD:** includere nella PR motivazione, verifiche svolte ed eventuali migration SQL.

```text
feat: add deck validation
fix: prevent duplicate card transfer
test: cover damage calculation edge cases
docs: update REST endpoint documentation
```

Le policy di branch, revisione, merge e protezione del repository sono definite separatamente dalle convenzioni di scrittura del codice.

## 14. Checklist Pull Request

Prima di richiedere una review, verificare:

- [ ] Codice, commenti, JavaDoc, errori e commit sono in inglese.
- [ ] Naming di classi, metodi, package, Enum e costanti è conforme.
- [ ] Non è stato utilizzato `var` né `Optional` fuori dai ritorni Repository.
- [ ] Non si restituiscono `null` o collection `null` dal codice applicativo.
- [ ] Lombok rispetta le annotazioni consentite; la dependency injection è via costruttore.
- [ ] Entity, colonne, relazioni e vincoli hanno mapping esplicito.
- [ ] Ogni modifica condivisa allo schema ha una nuova migration Flyway.
- [ ] I Controller utilizzano DTO e `ResponseEntity`, senza accedere ai Repository.
- [ ] Le validazioni strutturali e le regole business sono separate.
- [ ] Le transazioni sono circoscritte; nessuna chiamata HTTP avviene durante una transazione DB.
- [ ] Query e paginazione rispettano le convenzioni; non sono introdotte proiezioni o `@EntityGraph`.
- [ ] Gli endpoint pubblici modificati hanno documentazione Swagger/OpenAPI aggiornata (`@Operation`, risposte, DTO ed esempi verificati).
- [ ] Le nuove dipendenze sono motivate e approvate.
- [ ] Sono stati eseguiti i test pertinenti e non sono stati inseriti segreti.

---

**Nota:** questo file definisce soltanto le *convenzioni di programmazione*. Le decisioni su architettura, infrastruttura, autenticazione e sicurezza saranno documentate in file dedicati quando approvate.
