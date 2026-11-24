## Avvio locale con Docker

Il backend può essere avviato insieme a PostgreSQL senza installare Java, Maven, PostgreSQL o `keytool` sulla macchina host.

Dalla directory `backend`:

```powershell
.\setup-docker.ps1
docker compose up --build
```

Lo script chiede la password del keystore JWT e la salva nel file locale `.env`, escluso da Git. Il servizio `jwt-key-init` genera `jwt-signing.p12` nel volume Docker `jwt-keystore` solo se il file non esiste già. Il servizio `management-service` monta quel volume in sola lettura.

PostgreSQL usa il volume persistente `postgres-data`. Le migrazioni Flyway presenti in `Management-Service/src/main/resources/db/migration` vengono applicate automaticamente dall’applicazione al primo avvio e agli avvii successivi.

La migrazione inserisce automaticamente, dopo la creazione dello schema, i ruoli, il catalogo delle carte e i pacchetti di riferimento. Flyway la esegue al primo avvio del backend e la registra nello storico delle migrazioni.

Questi dati fanno parte del catalogo applicativo; utenti, wallet, acquisti e partite non vengono creati automaticamente.

Per rigenerare il keystore e inizializzare un database vuoto, eliminare esclusivamente i volumi del progetto dopo aver verificato che i dati non servano più:

```powershell
docker compose down -v
```
