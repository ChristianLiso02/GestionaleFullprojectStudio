# Gestionale FullProject Studio

Gestionale interno per la scuola di ballo **FullProject Studio** (salsa, bachata e affini), ad uso della segreteria per la gestione a 360° di:

- **Studenti** (anagrafica con codice fiscale obbligatorio e unico, contatti, note mediche, storico)
- **Istruttori** (anagrafica, specializzazioni, compenso orario)
- **Sale**
- **Corsi** (stile di ballo, livello, istruttore, sala, orari, capienza, prezzo mensile e prezzo della lezione singola)
- **Tipi di abbonamento** (mensile, trimestrale, annuale, pacchetti a lezioni)
- **Iscrizioni** (studente ↔ corso): si fanno una volta e restano attive finché lo studente non viene segnato come *ritirato* (riattivabile in qualsiasi momento)
- **Pagamenti** (metodo, mese di riferimento, mesi coperti, causale): un pagamento registrato è sempre un incasso effettivo
- **Lezioni singole**: chi non è iscritto può pagare una sola lezione (tipo di pagamento "Lezione singola", con corso e giorno della lezione, importo proposto dal prezzo del corso). Non crea iscrizioni né quote mensili, ma conta negli incassi del corso e della scuola e compare nell'Excel del corso e nel backup
- **Presenze** (appello per corso/data)
- **Quote mensili**: per ogni mese, chi ha pagato in tempo (entro il 7), in ritardo o non ha ancora pagato; il trimestrale copre 3 mesi
- **Dashboard** con statistiche (studenti attivi, incassi del mese, quote scadute da verificare)
- **Statistiche** mese per mese, per tutta la scuola e per singolo corso: iscritti attivi (uomini/donne), nuove iscrizioni, ritiri e rientri, incassi, puntualità delle quote, con grafici e tabella
- **Esportazione Excel di ogni corso** (pulsante "Scarica Excel" nel dettaglio corso): dati del corso, iscritti con contatti, griglia delle quote mese per mese, pagamenti, presenze e statistiche. Contiene dati personali e sanitari (note mediche): va conservato e condiviso con attenzione
- **Esporta dati per il server** (installazione su PC, menu Start): tutti i dati in `fullprojectstudio-dati.zip` e `fullprojectstudio-dati.sql` (SQL per PostgreSQL), sempre nella stessa cartella, pronti da importare nel server con `--trasferisci-da-h2` oppure con `psql`
- **Pagina Backup** (menu a sinistra, sezione Sistema): elenco dei backup con pulsante per scaricarli e per crearne uno subito
- **Profilo e impostazioni** dal menu che si apre cliccando sul proprio nome in alto a destra: modifica di nome, cognome ed email, cambio password
- **Backup Excel automatico** ogni notte, così la segreteria può continuare a lavorare da un file locale anche se il gestionale non fosse raggiungibile (vedi sezione dedicata)

Backend in **Java**, frontend statico (HTML/CSS/JS) — due progetti indipendenti.

## Architettura

```
backend/    API REST (Spring Boot, Spring Security + JWT, Spring Data JPA)
frontend/   Pagine statiche (HTML/CSS/JS vanilla) servite da un piccolo Spring Boot,
            senza controller/logica server-side: chiamano le API del backend
            direttamente dal browser con fetch()
```

Il frontend non ha accesso diretto al database né passa dal proprio server per i dati:
ogni pagina, una volta caricata, chiama le API REST del backend via `fetch()`. Il token
JWT ottenuto al login viene salvato nel `localStorage` del browser e allegato ad ogni
chiamata come header `Authorization: Bearer ...`. Il backend abilita CORS per l'origine
del frontend (`CORS_ALLOWED_ORIGINS`).

L'URL del backend usato dal browser (`API_BASE_URL` in `config.js`) è configurabile:
- **Con Docker**: variabile d'ambiente `API_BASE_URL` (letta da `docker-entrypoint.sh`, che
  rigenera `config.js` all'avvio del container — stessa immagine per locale e produzione)
- **Senza Docker** (`mvn spring-boot:run`): modifica direttamente
  `frontend/src/main/resources/static/js/config.js`

Colori del brand: **nero, bianco, rosso** (FullProject Studio).

## Requisiti

- Java 21+
- Maven 3.9+
- (facoltativo per produzione) PostgreSQL 14+

## Avvio in locale (sviluppo, database H2 su file)

Backend (porta **8080**):

```bash
cd backend
mvn spring-boot:run
```

Al primo avvio vengono creati automaticamente 2 utenti, con credenziali configurabili da
variabili d'ambiente (vedi tabella sotto) — se non impostate, valgono questi default di sviluppo:

- utente `admin` / `FullProject2026!` (ruolo ADMIN)
- utente `segreteria` / `Segreteria2026!` (ruolo SEGRETERIA)
- la stagione corrente (es. "2026/2027", calcolata dalla data di avvio)

Nessun dato di esempio: sale, istruttori, tipi di abbonamento e corsi li inserisce la segreteria
dal gestionale. Conviene partire da sale e istruttori, poi gli abbonamenti (es. "Mensile" con
durata 30 giorni, "Trimestrale" con durata 90 giorni: la durata decide quanti mesi copre la quota)
e infine i corsi.

**Prima di un uso reale con dati veri, cambia sempre `ADMIN_PASSWORD` e `SEGRETERIA_PASSWORD`**
tramite variabili d'ambiente — non lasciare quelle di default.

Frontend (porta **8081**):

```bash
cd frontend
mvn spring-boot:run
```

Poi apri il browser su **http://localhost:8081** ed effettua il login.

## Installazione sul PC della segreteria (Windows)

Un solo file, `FullProjectStudio-Setup.exe`, installa tutto (Java e database inclusi): il gestionale parte da
solo all'accensione del PC, si apre dall'icona sul desktop e fa da solo i backup (anche se il PC di notte è
spento). È raggiungibile solo da quel PC. L'installer lo crea GitHub Actions (workflow *Installer Windows*,
file `installer/`); guida completa in **[INSTALLAZIONE-PC.md](INSTALLAZIONE-PC.md)**.

In questa modalità backend e pagine sono un unico programma (profilo Maven `installer`, profilo Spring
`desktop`): database H2 in `C:\ProgramData\FullProjectStudio`, password iniziali casuali scritte in
`credenziali-iniziali.txt`, copia completa del database ogni notte con recupero all'accensione.

## Messa online su un server

Per l'uso reale il gestionale va installato su un server con `docker-compose.prod.yml`: HTTPS automatico
(Caddy), database non esposto su internet, riavvio automatico dei servizi, password lette dal file `.env`
(modello in `.env.example`) e backup notturni. La guida passo passo, per una VPS Aruba (con alternativa gratuita
su Oracle Cloud), è in **[INSTALLAZIONE-SERVER.md](INSTALLAZIONE-SERVER.md)**.

## Avvio con PostgreSQL

```bash
cd backend
mvn spring-boot:run -Dspring-boot.run.profiles=postgres \
  -DDB_URL=jdbc:postgresql://localhost:5432/fullprojectstudio \
  -DDB_USERNAME=fullprojectstudio -DDB_PASSWORD=fullprojectstudio
```

oppure imposta le variabili d'ambiente `DB_URL`, `DB_USERNAME`, `DB_PASSWORD` e avvia con
`SPRING_PROFILES_ACTIVE=postgres`.

## Backup Excel automatico

Ogni notte (di default alle **02:00**) il backend genera in automatico un file Excel
(`backup-AAAA-MM-GG.xlsx`, più una copia sempre aggiornata `backup-ultimo.xlsx`) con:

- **Quote da incassare** (primo foglio): tutte le quote scadute e quelle del mese ancora da rinnovare, con telefono dello studente, così la segreteria sa chi sollecitare anche a gestionale fermo
- anagrafiche complete: Studenti (con sesso), Istruttori, Sale, Stagioni, Corsi (con stagione), Abbonamenti
- Iscrizioni (con periodi di ritiro), Pagamenti (con mese di riferimento) e Presenze del **mese corrente e del mese precedente**

Il file viene scritto nella cartella indicata da `BACKUP_DIR` (default `./backup` in locale,
`/app/backup` nel container — mappata su `./backup` del PC host tramite `docker-compose.yml`).
Così, anche se il gestionale smettesse di funzionare, la segreteria può aprire quell'Excel
direttamente da Esplora File/Finder e continuare a lavorare con gli ultimi dati disponibili.

**Generare un backup subito** (senza aspettare la notte), da autenticati:
```bash
curl -X POST http://localhost:8080/api/backup/genera -H "Authorization: Bearer <token>"
```

Per cambiare l'orario, imposta `BACKUP_CRON` (formato cron Spring: secondi minuti ore giorno mese giorno-settimana),
es. `BACKUP_CRON=0 30 3 * * *` per le 03:30.

**Importante**: questo è un backup "per continuare a lavorare offline", non un vero disaster
recovery — non permette di ricostruire il database da zero. Per quello vedi la sezione seguente.

## Backup e ripristino del database (disaster recovery)

Oltre al backup Excel, il `docker-compose.yml` include un servizio `db-backup`
(immagine [`prodrigestivill/postgres-backup-local`](https://github.com/prodrigestivill/docker-postgres-backup-local))
che ogni notte esegue un dump completo del database PostgreSQL con `pg_dump`, in formato
compresso, con rotazione automatica (`BACKUP_KEEP_DAYS`/`_WEEKS`/`_MONTHS` in `docker-compose.yml`).

I file finiscono in `./backup/postgres/` sul PC host, organizzati per data:
```
backup/postgres/daily/fullprojectstudio-AAAAMMGG.sql.gz
backup/postgres/weekly/...
backup/postgres/monthly/...
```

A differenza del backup Excel (solo mese corrente/precedente, pensato per continuare a
lavorare se il gestionale è irraggiungibile), questo è un dump **completo** del database:
è il file da usare per ricostruire tutto da zero in caso di perdita totale (es. disco rotto,
volume Docker cancellato per errore).

**Generare un backup subito** (senza aspettare la notte):
```bash
docker run --rm -v "$(pwd)/backup/postgres:/backups" --network gestionalefullprojectstudio_default \
  -e POSTGRES_HOST=db -e POSTGRES_DB=fullprojectstudio \
  -e POSTGRES_USER=fullprojectstudio -e POSTGRES_PASSWORD=fullprojectstudio \
  prodrigestivill/postgres-backup-local /backup.sh
```

**Ripristinare da un backup** (procedura testata: dump → distruzione totale del database →
ripristino → verifica che l'app rilegga correttamente tutti i dati, incluse le relazioni
come i due istruttori per corso):

```bash
# 1. Ferma l'app (il db resta attivo)
docker compose stop backend frontend

# 2. Scompatta il backup più recente
gunzip -k backup/postgres/daily/fullprojectstudio-AAAAMMGG.sql.gz

# 3. Ricrea il database da zero
docker compose exec db psql -U fullprojectstudio -d postgres -c "DROP DATABASE fullprojectstudio;"
docker compose exec db psql -U fullprojectstudio -d postgres -c "CREATE DATABASE fullprojectstudio OWNER fullprojectstudio;"

# 4. Ripristina
docker compose exec -T db psql -U fullprojectstudio -d fullprojectstudio < backup/postgres/daily/fullprojectstudio-AAAAMMGG.sql

# 5. Riavvia l'app
docker compose start backend frontend
```

## Configurazione principale

| Variabile | Descrizione | Default |
|---|---|---|
| `JWT_SECRET` | Chiave di firma dei token JWT (backend) | valore di sviluppo incluso, **da cambiare in produzione** |
| `JWT_EXPIRATION_MS` | Durata del token JWT | 28800000 (8 ore) |
| `CORS_ALLOWED_ORIGINS` | Origini autorizzate a chiamare l'API (backend) | `http://localhost:8081` |
| `API_BASE_URL` | URL del backend visto dal browser (frontend, solo con Docker — vedi sopra) | `http://localhost:8080` |
| `ADMIN_USERNAME` / `ADMIN_PASSWORD` / `ADMIN_EMAIL` | Credenziali dell'utente ADMIN creato al primo avvio | `admin` / `FullProject2026!` / `admin@fullprojectstudio.it` — **da cambiare in produzione** |
| `SEGRETERIA_USERNAME` / `SEGRETERIA_PASSWORD` / `SEGRETERIA_EMAIL` | Credenziali dell'utente SEGRETERIA creato al primo avvio | `segreteria` / `Segreteria2026!` / `segreteria@fullprojectstudio.it` — **da cambiare in produzione** |
| `BACKUP_DIR` | Cartella dove scrivere i backup Excel | `./backup` |
| `BACKUP_CRON` | Orario di generazione automatica (formato cron) | `0 0 2 * * *` (ogni notte alle 02:00) |
| `LOGIN_MAX_TENTATIVI` / `LOGIN_MINUTI_BLOCCO` | Password sbagliate consentite (per utente e indirizzo) prima di bloccare il login, e durata del blocco in minuti | `5` / `15` |
| `PAGAMENTI_GIORNO_SCADENZA` | Ultimo giorno del mese in cui la quota non pagata è "da rinnovare"; dal giorno dopo è "scaduta" | `7` |
| `PAGAMENTI_MESI_SCADUTI_DA_VERIFICARE` | Mesi consecutivi scaduti dopo i quali lo studente compare in dashboard tra le quote da verificare (ha pagato o si è ritirato senza avvisare?) | `1` |

## Build

```bash
cd backend && mvn clean package
cd ../frontend && mvn clean package
```

Genera `backend/target/backend.jar` e `frontend/target/frontend.jar`, eseguibili con `java -jar`.
