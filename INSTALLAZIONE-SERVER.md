# Mettere online il gestionale (costo 0 €)

Guida passo passo per installare il gestionale su un server gratuito di **Oracle Cloud ("Always Free")**,
raggiungibile da qualsiasi PC, tablet o telefono all'indirizzo `https://<nome>.duckdns.org`.

Costo: **0 €**. Servono una carta di credito (Oracle la usa solo per verificare l'identità), un indirizzo
email e un'ora di tempo circa.

> Se un giorno si vuole un servizio con assistenza e garanzie, gli stessi passi valgono anche per un server
> a pagamento (es. Hetzner, circa 5 €/mese): cambia solo il punto 1–3, da "Collegarsi al server" in poi è identico.

---

## 1. Creare l'account Oracle Cloud

1. Vai su **oracle.com/cloud/free** → *Start for free*.
2. Compila i dati. Come **Home Region** scegli **Italy Northwest (Milan)** oppure **Germany Central (Frankfurt)**:
   i dati restano in Europa (GDPR). *La regione non si può cambiare dopo.*
3. Inserisci la carta per la verifica (Oracle può fare un addebito di prova di pochi centesimi, poi annullato).

### Consigliato: passare a "Pay As You Go" (resta gratis)

Nella console: menu ☰ → **Billing & Cost Management** → **Upgrade and Manage Payment** → *Pay As You Go*.

- Le risorse "Always Free" usate da questa guida **restano gratuite**.
- Senza questo passaggio Oracle può **spegnere e recuperare i server gratuiti poco usati** (un gestionale di
  segreteria usa poca CPU, quindi rischierebbe). Con Pay As You Go questo non succede.
- Per sicurezza crea un **avviso di spesa**: Billing → **Budgets** → *Create Budget* con importo **1 €** e la tua email.
  Se per errore si attivasse qualcosa a pagamento, ricevi subito una mail.

## 2. Creare il server

Menu ☰ → **Compute** → **Instances** → *Create instance*.

- **Name**: `gestionale`
- **Image**: *Change image* → **Canonical Ubuntu 24.04**
- **Shape**: *Change shape* → **Ampere** → `VM.Standard.A1.Flex` con **2 OCPU e 12 GB di memoria**
  (il limite gratuito è 4 OCPU e 24 GB in totale). Deve comparire l'etichetta *Always Free-eligible*.
- **Networking**: lascia le impostazioni proposte, con **Assign a public IPv4 address** attivo.
- **Add SSH keys**: *Generate a key pair for me* → **Save private key**. Conserva questo file (`.key`) con cura:
  è la chiave per entrare nel server.
- *Create*.

Se compare **"Out of capacity"**: in quel momento non ci sono server gratuiti liberi. Riprova più tardi, oppure
cambia *Availability domain*. Con Pay As You Go di solito si trova posto più facilmente.

Quando il server è **Running**, annota il **Public IP address** (es. `141.144.x.x`).

## 3. Aprire le porte del sito (80 e 443)

Nella pagina del server: **Primary VNIC → Subnet** → **Security Lists** → *Default Security List* →
*Add Ingress Rules*, due regole:

| Source CIDR | IP Protocol | Destination Port |
|---|---|---|
| `0.0.0.0/0` | TCP | `80` |
| `0.0.0.0/0` | TCP | `443` |

(La porta 22, per entrare con la chiave, è già aperta.) **Non** aprire la 5432 del database.

## 4. Scegliere l'indirizzo gratuito (DuckDNS)

1. Vai su **duckdns.org** ed entra (per esempio con l'account Google).
2. Scegli un nome libero, es. `fullprojectstudio` → *add domain*. L'indirizzo sarà `fullprojectstudio.duckdns.org`.
3. Nel campo **current ip** scrivi il Public IP del server e premi *update ip*.

> Se la scuola ha già un dominio (es. `fullprojectstudio.it`), si può usare invece un sottodominio come
> `gestionale.fullprojectstudio.it`: basta un record DNS di tipo **A** verso il Public IP.

## 5. Collegarsi al server

Dal PC (su Windows apri **PowerShell**, su Mac il **Terminale**), nella cartella dove hai salvato la chiave:

```bash
ssh -i nome-della-chiave.key ubuntu@141.144.x.x
```

Alla domanda *Are you sure you want to continue connecting* rispondi `yes`.
(Su Mac/Linux, se dice che la chiave ha permessi troppo aperti: `chmod 600 nome-della-chiave.key`.)

Tutti i comandi seguenti si scrivono **nel server**, cioè in questa finestra.

## 6. Preparare il server

```bash
# Aggiornamenti di sicurezza
sudo apt update && sudo apt -y upgrade

# Le immagini Ubuntu di Oracle hanno un firewall interno che blocca il sito: apriamo 80 e 443
sudo iptables -I INPUT 6 -m state --state NEW -p tcp --dport 80 -j ACCEPT
sudo iptables -I INPUT 6 -m state --state NEW -p tcp --dport 443 -j ACCEPT
sudo netfilter-persistent save

# Aggiornamenti di sicurezza automatici
sudo apt -y install unattended-upgrades

# Docker
curl -fsSL https://get.docker.com | sudo sh
sudo usermod -aG docker ubuntu
```

Poi **esci e rientra** (`exit`, e di nuovo il comando `ssh` del punto 5), così Docker funziona senza `sudo`.

## 7. Scaricare il gestionale

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

## 8. Impostare password e indirizzo

```bash
cp .env.example .env
openssl rand -base64 32   # lancialo 2 volte: una per DB_PASSWORD, una per JWT_SECRET
nano .env
```

Compila:

- `DOMINIO` = l'indirizzo del punto 4, senza `https://` (es. `fullprojectstudio.duckdns.org`)
- `DB_PASSWORD` e `JWT_SECRET` = i due codici generati sopra
- `ADMIN_PASSWORD` e `SEGRETERIA_PASSWORD` = le password con cui si entrerà nel gestionale
  (almeno 12 caratteri, non usate altrove)

Salva con **Ctrl+O**, Invio, ed esci con **Ctrl+X**. Conserva una copia di queste password in un posto sicuro
(es. un gestore di password): **senza `DB_PASSWORD` un backup del database non si può ripristinare.**

## 9. Avviare

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

## 10. Backup

Sul server, nella cartella `GestionaleFullprojectStudio/backup/`, si creano ogni notte:

- alle **02:00** l'Excel `backup-ultimo.xlsx` (più una copia datata): quote da incassare, anagrafiche, iscrizioni,
  pagamenti e presenze recenti, da aprire anche se il gestionale non funzionasse;
- alle **03:00** la copia completa del database in `backup/postgres/` (7 giorni, 4 settimane, 6 mesi): è quella
  che serve per ricostruire tutto.

**Importante: una copia deve stare anche fuori dal server.** Il modo più semplice, una volta alla settimana, dal
PC (PowerShell o Terminale, non dentro il server):

```bash
scp -r -i nome-della-chiave.key ubuntu@141.144.x.x:GestionaleFullprojectStudio/backup ./backup-gestionale
```

Il ripristino del database da una copia è spiegato nel README, sezione *Backup e ripristino del database*
(con `docker compose -f docker-compose.prod.yml` al posto di `docker compose`).

## 11. Aggiornare il gestionale

Quando ci sono modifiche nuove su GitHub:

```bash
cd GestionaleFullprojectStudio
git pull
docker compose -f docker-compose.prod.yml up -d --build
```

I dati non si toccano: stanno nel database, che resta com'è.

## 12. Vedere il database con DBeaver

Il database non è raggiungibile da internet: DBeaver ci arriva passando dalla connessione SSH.

- Nuova connessione **PostgreSQL**, scheda *Main*: Host `localhost`, Port `5432`, Database `fullprojectstudio`,
  Username `fullprojectstudio`, Password = `DB_PASSWORD` del file `.env`.
- Scheda **SSH**: *Use SSH Tunnel* ✔, Host = Public IP del server, Port `22`, User `ubuntu`,
  Authentication **Public Key**, Private key = il file `.key`.

## Problemi comuni

| Problema | Cosa fare |
|---|---|
| Il sito non si apre | Controlla le regole del punto 3 e i comandi `iptables` del punto 6; verifica su duckdns.org che l'IP sia quello del server |
| Il browser dice "non sicuro" | Il certificato si ottiene solo se il punto 3 e il punto 4 sono corretti: `docker compose -f docker-compose.prod.yml logs caddy` mostra il motivo |
| `required variable ... is missing a value` all'avvio | Manca un valore nel file `.env` (punto 8) |
| "Troppi tentativi sbagliati" al login | Dopo 5 password sbagliate il login di quell'utente si blocca per 15 minuti da quel dispositivo: aspetta, oppure entra da un'altra rete |
| Password dimenticata | Entra con l'altro utente (admin o segreteria), oppure chiedi assistenza: le password nel `.env` valgono solo al primo avvio |

Per cambiare password dall'interno del gestionale: pulsante **Cambia password** in alto a destra.
