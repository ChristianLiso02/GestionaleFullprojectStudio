# Installare il gestionale sul PC della segreteria (Windows)

Il gestionale si installa con un solo file, **`FullProjectStudio-Setup.exe`**: dentro c'è già tutto
(anche Java e il database), sul PC non va installato nient'altro.

Dopo l'installazione la segreteria **non deve fare più niente**: il gestionale parte da solo quando si accende
il PC, si apre con l'icona sul desktop e fa da solo i backup.

Il gestionale si usa **solo da quel PC** (non è raggiungibile da altri dispositivi né da internet).
Per usarlo anche da casa o dal telefono serve invece un server: vedi [INSTALLAZIONE-SERVER.md](INSTALLAZIONE-SERVER.md).

---

## 1. Scaricare l'installer (lo fai tu, non la segreteria)

L'installer lo crea GitHub in automatico, su un computer Windows, in circa 10 minuti:

1. Su GitHub apri il repository → scheda **Actions** → a sinistra **Installer Windows** → **Run workflow**
   (scegli il branch `claude/nice-pasteur-xwh577`) → **Run workflow**.
2. Quando la riga diventa verde ✔, aprila: in fondo, in **Artifacts**, c'è `FullProjectStudio-Setup-…`.
   Scaricalo: è uno ZIP che contiene `FullProjectStudio-Setup.exe`.

Durante la creazione GitHub prova anche il programma: lo avvia, fa un login, un backup, e lo installa.
Se qualcosa non va la riga diventa rossa e l'installer non viene prodotto.

> In alternativa, creando un tag di versione (`git tag v1.0.0 && git push origin v1.0.0`) l'installer
> compare nella pagina **Releases** del repository, sempre pronto da scaricare.

## 2. Installare (una volta sola, circa 5 minuti)

1. Copia `FullProjectStudio-Setup.exe` sul PC della segreteria (chiavetta, email, Drive) e fai doppio clic.
2. Windows può mostrare **"Windows ha protetto il PC"**: clicca **Ulteriori informazioni** → **Esegui comunque**.
   Succede perché l'installer non ha una firma digitale a pagamento: è normale.
3. Conferma con **Sì** la richiesta di permessi (serve un utente amministratore del PC).
4. **Cartella dei backup**: scegli una cartella sincronizzata con **Google Drive** o **OneDrive**, ad esempio
   `C:\Users\<nome>\Il mio Drive\Backup gestionale`. Così una copia dei dati resta al sicuro anche se il PC si
   rompe o viene rubato. Se sul PC non c'è Drive/OneDrive, lascia quella proposta (vedi punto 5).
5. **Installa** → **Fine**: il gestionale si apre nel browser.

Cosa trovi dopo l'installazione:

- l'icona **FullProject Studio** sul desktop e nel menu Start;
- nel menu Start → **FullProject Studio**: *Password iniziali*, *Cartella dati e backup*, *Disinstalla*.

## 3. Primo accesso

1. Menu Start → **FullProject Studio** → **Password iniziali**: si apre un file con utente e password
   (`admin` per te, `segreteria` per la segreteria), create a caso durante l'installazione.
2. Entra con `segreteria`, poi clicca sul tuo nome in alto a destra → **Impostazioni** e scegli una nuova password.
   Fai lo stesso con `admin`.
3. **Cancella il file** delle password iniziali (Start → *Cartella dati e backup* → `credenziali-iniziali.txt`).
4. Inserisci i dati di base, in quest'ordine: **sale**, **istruttori**, **abbonamenti** ("Mensile" con durata
   30 giorni, "Trimestrale" con durata 90 giorni), **corsi**, poi studenti e iscrizioni.

## 4. Uso di tutti i giorni

- Doppio clic sull'icona **FullProject Studio** sul desktop: si apre il gestionale nel browser.
- Il gestionale **parte da solo** quando si accende il PC e si entra in Windows: non c'è niente da avviare.
- Si può chiudere il browser quando si vuole: il gestionale resta acceso in sottofondo, i dati non si perdono.
- L'indirizzo è sempre **http://localhost:8081** (si può aggiungere ai preferiti).

## 5. Backup (automatici)

Nella cartella scelta all'installazione il gestionale salva:

| Cosa | Quando | A cosa serve |
|---|---|---|
| `backup-ultimo.xlsx` (+ una copia per ogni giorno) | ogni notte alle 02:00 | Excel con quote da incassare, anagrafiche, iscrizioni, pagamenti e presenze recenti: si apre anche se il gestionale non funzionasse |
| `database\database-<data>.zip` | ogni notte alle 03:00 | copia **completa** dei dati, per ricostruire tutto; si tengono le ultime 30 |

**Se il PC di notte è spento** (il caso più comune) non si perde niente: all'accensione, dopo pochi minuti,
il gestionale si accorge che manca il backup e lo fa subito.

Tutti i backup si vedono anche dentro il gestionale, nella pagina **Backup** (menu a sinistra, in fondo):
da lì si possono scaricare con un clic o crearne uno subito.

**Se la cartella non è su Drive/OneDrive**, una volta alla settimana copia la cartella dei backup su una
chiavetta (menu Start → FullProject Studio → *Cartella dati e backup* → cartella `backup`).

## 6. Aggiornare il gestionale

Quando ci sono novità, scarica il nuovo `FullProjectStudio-Setup.exe` (punto 1) e installalo **sopra** quello
vecchio, con doppio clic. **I dati restano dove sono**: l'installer chiude il gestionale, lo aggiorna e lo riapre.
La cartella dei backup non viene richiesta di nuovo.

## 7. Recuperare i dati da un backup

Serve solo se i dati si sono rovinati o se si passa a un PC nuovo.

1. Chiudi il gestionale: **Gestione attività** (Ctrl+Maiusc+Esc) → *FullProjectStudio* → **Termina attività**.
2. Apri `C:\ProgramData\FullProjectStudio\db` e sposta altrove (non cancellare) i file che contiene.
3. Apri lo ZIP del backup scelto (`database-<data>.zip`) e copia il file `fullprojectstudio.mv.db` in quella cartella.
4. Riavvia il PC (o fai doppio clic sull'icona del gestionale).

Su un **PC nuovo**: prima installa il gestionale (punto 2), poi segui questi passi.

## Passare a un server in futuro

Se un domani il gestionale deve essere usato anche da casa o dal telefono, si sposta su un server
([INSTALLAZIONE-SERVER.md](INSTALLAZIONE-SERVER.md)) **portandosi dietro tutti i dati**, compresi utenti e password.

Per farlo c'è **Esporta dati per il server** (menu Start → FullProject Studio): copia tutti i dati nel file
`fullprojectstudio-dati.zip`, **sempre nella stessa cartella** (`C:\ProgramData\FullProjectStudio\migrazione`), sostituendo
ogni volta l'export precedente. Funziona anche con il gestionale acceso e si può lanciare quante volte si vuole:
l'ultimo export è quello da portare sul server (punto *6-bis* di quella guida).

- **Cartella diversa** (per esempio Google Drive): copia `C:\Program Files\FullProject Studio\Esporta dati per il server.bat`
  sul desktop, aprila con il Blocco note e cambia la riga `set "DEST=..."`; da quel momento usa la copia.
  (Così gli aggiornamenti del gestionale non cancellano la tua modifica.)
- **In automatico** (per avere sempre un export aggiornato): *Utilità di pianificazione* di Windows → *Crea attività
  di base* → ogni giorno → *Avvio programma* → il file `.bat` (quello originale o la tua copia), con argomento `/silenzioso`.

## Impostazioni avanzate

Nel file `C:\ProgramData\FullProjectStudio\config\application.properties` (si apre con il Blocco note) si possono
cambiare la cartella dei backup, la porta, quante copie del database tenere e il giorno di scadenza delle quote.
Dopo una modifica riavvia il PC.

Il **database** si può aprire con DBeaver scegliendo **H2 Embedded**, con URL
`jdbc:h2:file:C:/ProgramData/FullProjectStudio/db/fullprojectstudio;AUTO_SERVER=TRUE`, utente `sa`, password vuota
(funziona anche con il gestionale acceso).

I **log** (utili per l'assistenza) sono in `C:\ProgramData\FullProjectStudio\log\gestionale.log`.

## Problemi comuni

| Problema | Cosa fare |
|---|---|
| "Windows ha protetto il PC" | *Ulteriori informazioni* → *Esegui comunque* (punto 2) |
| L'antivirus blocca l'installer | Consenti il file: è lo stesso prodotto da GitHub, senza firma a pagamento |
| Doppio clic sull'icona ma non si apre niente | Aspetta 30 secondi e riprova: alla prima accensione il gestionale impiega qualche secondo a partire |
| Il browser dice "Impossibile raggiungere il sito" | Riavvia il PC; se persiste, manda a chi fa assistenza il file `gestionale.log` |
| Password dimenticata | Entra con l'altro utente (admin o segreteria) e fatti aiutare; oppure contatta l'assistenza |
| "Troppi tentativi sbagliati" | Dopo 5 password sbagliate il login si blocca per 15 minuti: aspetta e riprova |
