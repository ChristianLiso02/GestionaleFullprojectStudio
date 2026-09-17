# Gestionale FullProject Studio

Gestionale interno per la scuola di ballo **FullProject Studio** (salsa, bachata e affini), ad uso della segreteria per la gestione a 360° di:

- **Studenti** (anagrafica, contatti, note mediche, storico)
- **Istruttori** (anagrafica, specializzazioni, compenso orario)
- **Sale**
- **Corsi** (stile di ballo, livello, istruttore, sala, orari, capienza, prezzo)
- **Tipi di abbonamento** (mensile, trimestrale, annuale, pacchetti a lezioni)
- **Iscrizioni** (studente ↔ corso, con scadenza calcolata dall'abbonamento)
- **Pagamenti** (metodo, stato, causale)
- **Presenze** (appello per corso/data)
- **Dashboard** con statistiche (studenti attivi, incassi del mese, iscrizioni in scadenza, pagamenti in sospeso)
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

Per cambiare l'URL del backend visto dal browser, modifica
`frontend/src/main/resources/static/js/config.js` (`API_BASE_URL`).

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
- 2 sale, 2 istruttori, 4 tipi di abbonamento, 2 corsi di esempio

**Prima di un uso reale con dati veri, cambia sempre `ADMIN_PASSWORD` e `SEGRETERIA_PASSWORD`**
tramite variabili d'ambiente — non lasciare quelle di default.

Frontend (porta **8081**):

```bash
cd frontend
mvn spring-boot:run
```

Poi apri il browser su **http://localhost:8081** ed effettua il login.

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

- anagrafiche complete: Studenti, Istruttori, Sale, Corsi, Abbonamenti
- Iscrizioni, Pagamenti e Presenze del **mese corrente e del mese precedente**

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

## Configurazione principale

| Variabile | Descrizione | Default |
|---|---|---|
| `JWT_SECRET` | Chiave di firma dei token JWT (backend) | valore di sviluppo incluso, **da cambiare in produzione** |
| `JWT_EXPIRATION_MS` | Durata del token JWT | 28800000 (8 ore) |
| `CORS_ALLOWED_ORIGINS` | Origini autorizzate a chiamare l'API (backend) | `http://localhost:8081` |
| `API_BASE_URL` (in `frontend/.../js/config.js`, non è una env var) | URL del backend visto dal browser | `http://localhost:8080` |
| `ADMIN_USERNAME` / `ADMIN_PASSWORD` / `ADMIN_EMAIL` | Credenziali dell'utente ADMIN creato al primo avvio | `admin` / `FullProject2026!` / `admin@fullprojectstudio.it` — **da cambiare in produzione** |
| `SEGRETERIA_USERNAME` / `SEGRETERIA_PASSWORD` / `SEGRETERIA_EMAIL` | Credenziali dell'utente SEGRETERIA creato al primo avvio | `segreteria` / `Segreteria2026!` / `segreteria@fullprojectstudio.it` — **da cambiare in produzione** |
| `BACKUP_DIR` | Cartella dove scrivere i backup Excel | `./backup` |
| `BACKUP_CRON` | Orario di generazione automatica (formato cron) | `0 0 2 * * *` (ogni notte alle 02:00) |

## Build

```bash
cd backend && mvn clean package
cd ../frontend && mvn clean package
```

Genera `backend/target/backend.jar` e `frontend/target/frontend.jar`, eseguibili con `java -jar`.
