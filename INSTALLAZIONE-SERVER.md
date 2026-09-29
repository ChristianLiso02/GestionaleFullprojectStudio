# Mettere online il gestionale

Guida passo passo per installare il gestionale su un server, raggiungibile da qualsiasi PC, tablet o telefono
all'indirizzo `https://<indirizzo>`.

La guida è scritta per una **VPS Aruba** (server italiano, pochi euro al mese, pagabile anche con bonifico o PayPal).
Chi preferisce un server gratuito trova in fondo l'**Appendice: Oracle Cloud** (richiede una carta di credito):
cambia solo la creazione del server, tutto il resto è uguale.

Serve circa un'ora. Le schermate dei siti dei fornitori cambiano spesso: se un nome di pulsante è leggermente
diverso, cerca quello più simile.

---

## 1. Ordinare la VPS su Aruba

Serve una **VPS** (su Aruba: *Cloud VPS*, dal sito **cloud.aruba.it**).
**Non** va bene l'*Hosting* classico (quello per i siti web): non permette di installare Docker.

1. Su **cloud.aruba.it** scegli **Cloud VPS** e registra l'account.
2. Configurazione consigliata:
   - **2 vCPU e 4 GB di RAM** (con 2 GB funziona ma è al limite), almeno **40 GB di disco**
   - **Sistema operativo: Ubuntu 24.04** (Linux, non Windows)
   - **Data center: Italia**
3. Alla fine dell'ordine, come metodo di pagamento scegli quello che preferisci tra quelli offerti (carta, PayPal,
   bonifico: verifica al momento dell'ordine quali sono disponibili). La fattura può essere intestata alla scuola.
4. Imposta una **password lunga** per l'utente `root` (o segui le istruzioni del pannello se genera una chiave SSH).
   Conservala in un gestore di password.
5. Quando la VPS è attiva, dal pannello (o dall'email di conferma) annota il **indirizzo IP** (es. `95.110.x.x`).

Se nel pannello Aruba è attivo un **firewall**, apri le porte **22**, **80** e **443**. **Non** aprire la 5432 del database.

## 2. Scegliere l'indirizzo del gestionale

**Gratuito, con DuckDNS**

1. Vai su **duckdns.org** ed entra (per esempio con l'account Google).
2. Scegli un nome libero, es. `fullprojectstudio` → *add domain*. L'indirizzo sarà `fullprojectstudio.duckdns.org`.
3. Nel campo **current ip** scrivi l'IP della VPS e premi *update ip*.

**Con un dominio della scuola** (es. `fullprojectstudio.it`): nel pannello dei DNS del dominio crea un record di tipo
**A** con nome `gestionale` che punta all'IP della VPS. L'indirizzo sarà `gestionale.fullprojectstudio.it`.
Il cambio può impiegare da pochi minuti a qualche ora.

## 3. Collegarsi al server

Dal PC (su Windows apri **PowerShell**, su Mac il **Terminale**):

```bash
ssh root@95.110.x.x
```

Alla domanda *Are you sure you want to continue connecting* rispondi `yes`, poi scrivi la password del punto 1
(mentre la scrivi non compare niente: è normale).

Tutti i comandi seguenti si scrivono **nel server**, cioè in questa finestra.

## 4. Preparare il server

```bash
# Aggiornamenti di sicurezza, anche automatici in futuro
apt update && apt -y upgrade
apt -y install unattended-upgrades fail2ban git

# fail2ban blocca chi prova a indovinare la password del server

# Docker
curl -fsSL https://get.docker.com | sh
docker run --rm hello-world   # deve stampare "Hello from Docker!"
```

**Solo se la VPS ha 2 GB di RAM**, aggiungi memoria di appoggio (serve alla prima compilazione):

```bash
fallocate -l 2G /swapfile && chmod 600 /swapfile && mkswap /swapfile && swapon /swapfile
echo '/swapfile none swap sw 0 0' >> /etc/fstab
```

Da qui in poi useremo un utente normale invece di `root`, più sicuro:

```bash
adduser gestionale            # scegli una password e premi Invio alle altre domande
usermod -aG sudo,docker gestionale
exit
```

Ora rientra come `gestionale` (`ssh gestionale@95.110.x.x`): è l'utente che userai sempre da ora in poi.

## 5. Scaricare il gestionale

Il repository è privato, quindi serve un "token" GitHub di sola lettura:

1. Su GitHub: foto profilo → **Settings** → **Developer settings** → **Personal access tokens** →
   **Fine-grained tokens** → *Generate new token*.
2. *Repository access*: **Only select repositories** → `GestionaleFullprojectStudio`.
   *Permissions* → *Contents*: **Read-only**. Scadenza a piacere.
3. Copia il token.

Nel server:

```bash
git clone -b claude/nice-pasteur-xwh577 https://github.com/ChristianLiso02/GestionaleFullprojectStudio.git
cd GestionaleFullprojectStudio
```

Quando chiede *Username* scrivi il tuo utente GitHub, come *Password* incolla il **token**.

## 6. Impostare password e indirizzo

```bash
cp .env.example .env
openssl rand -base64 32   # lancialo 2 volte: una per DB_PASSWORD, una per JWT_SECRET
nano .env
```

Compila:

- `DOMINIO` = l'indirizzo del punto 2, senza `https://` (es. `fullprojectstudio.duckdns.org`)
- `DB_PASSWORD` e `JWT_SECRET` = i due codici generati sopra
- `ADMIN_PASSWORD` e `SEGRETERIA_PASSWORD` = le password con cui si entrerà nel gestionale
  (almeno 12 caratteri, non usate altrove)

Salva con **Ctrl+O**, Invio, ed esci con **Ctrl+X**. Conserva una copia di queste password in un posto sicuro
(es. un gestore di password): **senza `DB_PASSWORD` un backup del database non si può ripristinare.**

## 6-bis. Solo se il gestionale era già in uso sul PC: portare i dati sul server

Se la scuola ha usato fino a oggi il gestionale installato sul PC ([INSTALLAZIONE-PC.md](INSTALLAZIONE-PC.md)),
i dati si trasferiscono tutti sul server: anagrafiche, iscrizioni, pagamenti, presenze e anche **gli utenti con
le loro password** (si entra con le stesse di prima; `ADMIN_PASSWORD` e `SEGRETERIA_PASSWORD` del `.env` non
verranno usate). Va fatto **prima** del primo avvio del punto 7.

1. Sul PC della scuola, **a fine giornata** (quello che si registra dopo non verrà trasferito): menu Start →
   FullProject Studio → **Esporta dati per il server**. Si apre una finestra che copia tutti i dati nel file
   due file, sempre nella stessa cartella (`C:\ProgramData\FullProjectStudio\migrazione`):
   `fullprojectstudio-dati.zip` (per il trasferimento automatico) e `fullprojectstudio-dati.sql` (SQL per PostgreSQL,
   leggibile con qualsiasi editor o con DBeaver). Alla fine la finestra mostra quanti studenti, iscrizioni,
   pagamenti… ha esportato e apre la cartella.
2. Dal PC copia il file sul server:
   ```bash
   scp "C:\ProgramData\FullProjectStudio\migrazione\fullprojectstudio-dati.zip" gestionale@95.110.x.x:GestionaleFullprojectStudio/
   ```
3. Nel server:
   ```bash
   cd GestionaleFullprojectStudio
   docker compose -f docker-compose.prod.yml run --rm -v "$PWD:/import" backend --trasferisci-da-h2=/import/fullprojectstudio-dati.zip
   ```
   Alla fine deve comparire **TRASFERIMENTO COMPLETATO** con il numero di righe copiate per ogni tabella.
   (Al posto dell'export si può usare anche un backup notturno `database-<data>.zip`: il comando è lo stesso.)

   **In alternativa, con il file SQL.** Il file `fullprojectstudio-dati.sql` contiene solo comandi `INSERT` in
   sintassi PostgreSQL. Le tabelle le crea il gestionale al primo avvio, quindi si importa **dopo** aver fatto
   partire il gestionale una volta (punto 7):
   ```bash
   scp "C:\ProgramData\FullProjectStudio\migrazione\fullprojectstudio-dati.sql" gestionale@95.110.x.x:GestionaleFullprojectStudio/
   cd GestionaleFullprojectStudio
   docker compose -f docker-compose.prod.yml exec -T db psql -v ON_ERROR_STOP=1 -U fullprojectstudio -d fullprojectstudio < fullprojectstudio-dati.sql
   ```
   Il file sostituisce gli utenti e la stagione creati al primo avvio con quelli esportati (si entra con le stesse
   password di prima). Se il server contiene già dei dati, l'importazione **si ferma senza modificare niente**
   (`Il database del server contiene già dei dati: importazione annullata`). Se c'è un errore a metà, non resta
   niente di importato a metà. Contiene dati personali e sanitari: cancellalo dal server a importazione finita.
4. Da quel momento si lavora **solo sul server**: sul PC disinstalla il gestionale (o almeno non usarlo più),
   così nessuno registra dati nel posto sbagliato. I suoi backup restano comunque nella cartella scelta.

Il trasferimento si rifiuta di partire se sul server ci sono già dati, per non mescolarli. Se il server era già
stato avviato per prova, prima si svuota con `docker compose -f docker-compose.prod.yml down -v`
(**cancella tutti i dati del server**) e poi si ripete il punto 3.

## 7. Avviare

```bash
docker compose -f docker-compose.prod.yml up -d --build
```

La prima volta ci mette **10–15 minuti** (scarica e compila tutto). Per vedere se è pronto:

```bash
docker compose -f docker-compose.prod.yml ps
docker compose -f docker-compose.prod.yml logs -f backend   # Ctrl+C per uscire
```

Quando nei log compare `Started BackendApplication`, apri il browser su **`https://<DOMINIO>`** ed entra con
`segreteria` e la password scelta. Il lucchetto HTTPS arriva da solo entro un minuto dal primo avvio.

Da questo momento il gestionale **si riavvia da solo** se il server viene riavviato o se un servizio si blocca.

## 8. Backup

Sul server, nella cartella `GestionaleFullprojectStudio/backup/`, si creano ogni notte:

- alle **02:00** l'Excel `backup-ultimo.xlsx` (più una copia datata): quote da incassare, anagrafiche, iscrizioni,
  pagamenti e presenze recenti, da aprire anche se il gestionale non funzionasse;
- alle **03:00** la copia completa del database in `backup/postgres/` (7 giorni, 4 settimane, 6 mesi): è quella
  che serve per ricostruire tutto.

**Importante: una copia deve stare anche fuori dal server.** Il modo più semplice: nel gestionale, pagina
**Backup** (menu a sinistra, in fondo) → **Scarica** sul backup più recente, una volta alla settimana.
In alternativa, dal PC (PowerShell o Terminale, non dentro il server) si scarica tutta la cartella:

```bash
scp -r gestionale@95.110.x.x:GestionaleFullprojectStudio/backup ./backup-gestionale
```

Il ripristino del database da una copia è spiegato nel README, sezione *Backup e ripristino del database*
(con `docker compose -f docker-compose.prod.yml` al posto di `docker compose`).

In più, dal pannello Aruba si può attivare il **backup della VPS** (a pagamento, di solito pochi euro al mese):
è una protezione in più, non sostituisce la copia sul tuo PC.

## 9. Aggiornare il gestionale

Quando ci sono modifiche nuove su GitHub:

```bash
cd GestionaleFullprojectStudio
git pull
docker compose -f docker-compose.prod.yml up -d --build
```

I dati non si toccano: stanno nel database, che resta com'è.

## 10. Vedere il database con DBeaver

Il database non è raggiungibile da internet: DBeaver ci arriva passando dalla connessione SSH.

- Nuova connessione **PostgreSQL**, scheda *Main*: Host `localhost`, Port `5432`, Database `fullprojectstudio`,
  Username `fullprojectstudio`, Password = `DB_PASSWORD` del file `.env`.
- Scheda **SSH**: *Use SSH Tunnel* ✔, Host = IP del server, Port `22`, User `gestionale`,
  Authentication **Password** (quella scelta al punto 4).

## Problemi comuni

| Problema | Cosa fare |
|---|---|
| Il sito non si apre | Controlla che il firewall del pannello (se attivo) lasci aperte 80 e 443; verifica su duckdns.org (o nei DNS del dominio) che l'IP sia quello del server |
| Il browser dice "non sicuro" | Il certificato si ottiene solo se le porte 80 e 443 sono aperte e l'indirizzo punta al server: `docker compose -f docker-compose.prod.yml logs caddy` mostra il motivo |
| `required variable ... is missing a value` all'avvio | Manca un valore nel file `.env` (punto 6) |
| `permission denied` usando docker | Sei entrato come `root` o come un utente non aggiunto al gruppo docker: esci e rientra come `gestionale` |
| "Troppi tentativi sbagliati" al login | Dopo 5 password sbagliate il login di quell'utente si blocca per 15 minuti da quel dispositivo: aspetta, oppure entra da un'altra rete |
| Password dimenticata | Entra con l'altro utente (admin o segreteria), oppure chiedi assistenza: le password nel `.env` valgono solo al primo avvio |

Per cambiare password dall'interno del gestionale: clicca sul tuo nome in alto a destra → **Impostazioni**.

---

## Appendice: server gratuito su Oracle Cloud ("Always Free")

Alternativa a costo **0 €**. Richiede una **carta di credito** (Oracle la usa per verificare l'identità) e un po'
di pazienza: a volte i server gratuiti sono esauriti. Segui questi passi **al posto dei punti 1, 3 e 4** della guida
sopra; tutti gli altri punti restano uguali, tranne che l'utente del server è `ubuntu` invece di `gestionale`
(e non serve creare un nuovo utente).

### O1. Account

1. Vai su **oracle.com/cloud/free** → *Start for free*.
2. Come **Home Region** scegli **Italy Northwest (Milan)** oppure **Germany Central (Frankfurt)**:
   i dati restano in Europa (GDPR). *La regione non si può cambiare dopo.*
3. Inserisci la carta per la verifica (Oracle può fare un addebito di prova di pochi centesimi, poi annullato).

**Consigliato: passare a "Pay As You Go" (resta gratis).** Menu ☰ → **Billing & Cost Management** →
**Upgrade and Manage Payment**. Le risorse "Always Free" restano gratuite, ma senza questo passaggio Oracle può
**spegnere e recuperare i server poco usati**. Crea anche un **avviso di spesa**: Billing → **Budgets** → importo
**1 €** con la tua email.

### O2. Server

Menu ☰ → **Compute** → **Instances** → *Create instance*.

- **Image**: **Canonical Ubuntu 24.04**
- **Shape**: **Ampere** → `VM.Standard.A1.Flex` con **2 OCPU e 12 GB** (deve comparire *Always Free-eligible*)
- **Add SSH keys**: *Generate a key pair for me* → **Save private key** (file `.key`, da conservare con cura)

Se compare **"Out of capacity"**, riprova più tardi o cambia *Availability domain*.
Quando il server è **Running**, annota il **Public IP address**.

### O3. Porte del sito

Pagina del server → **Primary VNIC → Subnet** → **Security Lists** → *Default Security List* → *Add Ingress Rules*:
`0.0.0.0/0`, TCP, porta **80**, e un'altra regola con porta **443**. Non aprire la 5432.

### O4. Collegarsi e preparare

```bash
ssh -i nome-della-chiave.key ubuntu@<Public IP>
```

(Su Mac/Linux, se dice che la chiave ha permessi troppo aperti: `chmod 600 nome-della-chiave.key`.)
Poi, al posto del punto 4 della guida:

```bash
sudo apt update && sudo apt -y upgrade
sudo apt -y install unattended-upgrades
# Le immagini Oracle hanno un firewall interno che blocca il sito: apriamo 80 e 443
sudo iptables -I INPUT 6 -m state --state NEW -p tcp --dport 80 -j ACCEPT
sudo iptables -I INPUT 6 -m state --state NEW -p tcp --dport 443 -j ACCEPT
sudo netfilter-persistent save
curl -fsSL https://get.docker.com | sudo sh
sudo usermod -aG docker ubuntu
```

Esci e rientra (`exit` e di nuovo `ssh`), poi prosegui dal **punto 5**. Per `scp` e DBeaver usa `-i nome-della-chiave.key`
e l'utente `ubuntu` (in DBeaver: Authentication **Public Key** con il file `.key`).
