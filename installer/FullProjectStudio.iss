; Installer Windows di FullProject Studio (Inno Setup 6).
; Lo compila il workflow .github/workflows/installer.yml:
;   iscc /DAppVersion=1.0.0 /DAppImage=<cartella del programma creata da jpackage> FullProjectStudio.iss
;
; Cosa fa:
; - installa il programma (con Java incluso) in Programmi\FullProject Studio
; - crea la cartella dei dati C:\ProgramData\FullProjectStudio (database, backup, log, impostazioni):
;   NON viene toccata da aggiornamenti e disinstallazione, così i dati non si perdono mai
; - icona sul desktop e nel menu Start, avvio automatico all'accesso a Windows
; - alla prima installazione chiede in quale cartella salvare i backup
; - "Esporta dati per il server" (menu Start): tutti i dati in un file, per il passaggio a PostgreSQL

#ifndef AppVersion
  #define AppVersion "1.0.0"
#endif
#ifndef AppImage
  #define AppImage "..\build\app\FullProjectStudio"
#endif

#define AppName "FullProject Studio"
#define AppExe "FullProjectStudio.exe"
#define DataDir "{commonappdata}\FullProjectStudio"

[Setup]
AppId={{7B0E9C8A-4D2F-4B8E-9A61-3C5F2E7D1A90}
AppName={#AppName}
AppVersion={#AppVersion}
AppVerName={#AppName} {#AppVersion}
AppPublisher=FullProject Studio
DefaultDirName={autopf}\{#AppName}
DefaultGroupName={#AppName}
DisableProgramGroupPage=yes
DisableDirPage=yes
PrivilegesRequired=admin
ArchitecturesAllowed=x64compatible
ArchitecturesInstallIn64BitMode=x64compatible
OutputDir=..\build
OutputBaseFilename=FullProjectStudio-Setup
SetupIconFile=fullprojectstudio.ico
UninstallDisplayIcon={app}\{#AppExe}
UninstallDisplayName={#AppName}
Compression=lzma2
SolidCompression=yes
WizardStyle=modern
; Chiude il gestionale se è acceso durante un aggiornamento
CloseApplications=force
RestartApplications=no

[Languages]
Name: "italian"; MessagesFile: "compiler:Languages\Italian.isl"

[Messages]
FinishedLabel=FullProject Studio è installato e partirà da solo ogni volta che si accende il PC.%n%nPer entrare la prima volta, le password sono nel menu Start > FullProject Studio > Password iniziali. Dopo il primo accesso cambiale con il pulsante "Cambia password".

[Dirs]
; Tutti gli utenti del PC devono poter scrivere dati e backup
Name: "{#DataDir}"; Permissions: users-modify
Name: "{#DataDir}\config"; Permissions: users-modify

[Files]
Source: "{#AppImage}\*"; DestDir: "{app}"; Flags: ignoreversion recursesubdirs createallsubdirs
Source: "fullprojectstudio.ico"; DestDir: "{app}"; Flags: ignoreversion
Source: "Esporta dati per il server.bat"; DestDir: "{app}"; Flags: ignoreversion

[Icons]
Name: "{autodesktop}\{#AppName}"; Filename: "{app}\{#AppExe}"; Parameters: "--apri"; IconFilename: "{app}\fullprojectstudio.ico"
Name: "{group}\{#AppName}"; Filename: "{app}\{#AppExe}"; Parameters: "--apri"; IconFilename: "{app}\fullprojectstudio.ico"
Name: "{group}\Password iniziali"; Filename: "{#DataDir}\credenziali-iniziali.txt"
Name: "{group}\Cartella dati e backup"; Filename: "{#DataDir}"
; Esporta tutti i dati in un file per il passaggio al server (sempre stesso file, sostituito ogni volta)
Name: "{group}\Esporta dati per il server"; Filename: "{app}\Esporta dati per il server.bat"; WorkingDir: "{app}"; IconFilename: "{app}\fullprojectstudio.ico"
Name: "{group}\Disinstalla {#AppName}"; Filename: "{uninstallexe}"
; Avvio automatico all'accesso a Windows, senza aprire il browser
Name: "{commonstartup}\{#AppName}"; Filename: "{app}\{#AppExe}"; IconFilename: "{app}\fullprojectstudio.ico"

[Run]
Filename: "{app}\{#AppExe}"; Parameters: "--apri"; Description: "Apri FullProject Studio adesso"; Flags: nowait postinstall skipifsilent runasoriginaluser

[UninstallRun]
Filename: "{sys}\taskkill.exe"; Parameters: "/F /IM {#AppExe}"; Flags: runhidden; RunOnceId: "FermaGestionale"

[Code]
var
  PaginaBackup: TInputDirWizardPage;

function FileConfigurazione(): String;
begin
  Result := ExpandConstant('{#DataDir}\config\application.properties');
end;

procedure InitializeWizard();
begin
  PaginaBackup := CreateInputDirPage(wpReady - 1,
    'Cartella dei backup',
    'Dove salvare le copie di sicurezza dei dati?',
    'Ogni giorno il gestionale salva qui una copia completa dei dati e un file Excel.' + #13#10 + #13#10 +
    'Consigliato: una cartella sincronizzata con Google Drive o OneDrive (ad esempio "Il mio Drive\Backup gestionale"), ' +
    'così una copia resta al sicuro anche se il PC si rompe.' + #13#10 + #13#10 +
    'Se non sai cosa scegliere, lascia quella proposta: si potrà cambiare in seguito.',
    False, '');
  PaginaBackup.Add('');
  PaginaBackup.Values[0] := ExpandConstant('{#DataDir}\backup');
end;

// Negli aggiornamenti la cartella è già stata scelta: la pagina non si mostra.
function ShouldSkipPage(PageID: Integer): Boolean;
begin
  Result := (PageID = PaginaBackup.ID) and FileExists(FileConfigurazione());
end;

// Prima di sostituire i file ferma il gestionale, se è acceso.
function PrepareToInstall(var NeedsRestart: Boolean): String;
var
  Codice: Integer;
begin
  Exec(ExpandConstant('{sys}\taskkill.exe'), '/F /IM {#AppExe}', '', SW_HIDE, ewWaitUntilTerminated, Codice);
  Result := '';
end;

procedure CurStepChanged(CurStep: TSetupStep);
var
  Cartella, Testo: String;
begin
  if (CurStep = ssPostInstall) and not FileExists(FileConfigurazione()) then
  begin
    // Nel file delle impostazioni le "\" vanno scritte come "/"
    Cartella := PaginaBackup.Values[0];
    StringChangeEx(Cartella, '\', '/', True);
    Testo :=
      '# Impostazioni di FullProject Studio. Dopo una modifica riavvia il PC (o il gestionale).' + #13#10 +
      '#' + #13#10 +
      '# Cartella dove salvare i backup (usa / al posto di \)' + #13#10 +
      'app.backup.dir=' + Cartella + #13#10 +
      '#' + #13#10 +
      '# Altre impostazioni possibili (togli il # davanti per attivarle):' + #13#10 +
      '# server.port=8765' + #13#10 +
      '# app.backup.copie-database-da-tenere=30' + #13#10 +
      '# app.pagamenti.giorno-scadenza=7' + #13#10;
    SaveStringToFile(FileConfigurazione(), Testo, False);
  end;
end;
