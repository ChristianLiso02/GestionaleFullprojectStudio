@echo off
rem ==========================================================================
rem  FullProject Studio - Esporta dati per il server
rem
rem  Copia TUTTI i dati del gestionale (anagrafiche, iscrizioni, pagamenti,
rem  presenze, utenti e password) nel file fullprojectstudio-dati.zip, sempre
rem  nella stessa cartella: ogni volta l'export precedente viene sostituito.
rem  Quel file e' quello da portare sul server per passare a PostgreSQL
rem  (INSTALLAZIONE-SERVER.md, punto 6-bis).
rem
rem  Funziona anche con il gestionale acceso. Si puo' lanciare quante volte si
rem  vuole, anche in automatico (Utilita' di pianificazione di Windows).
rem ==========================================================================

rem Cartella dove salvare l'export. Per metterlo su Google Drive o OneDrive
rem cambia la riga qui sotto, ad esempio:
rem   set "DEST=%USERPROFILE%\Il mio Drive\Gestionale - export per server"
set "DEST=%ProgramData%\FullProjectStudio\migrazione"

chcp 65001 >nul
title FullProject Studio - Esportazione dati per il server

rem Il programma e' accanto a questo file (installazione); altrimenti nella cartella standard.
set "APP=%~dp0FullProjectStudio.exe"
if not exist "%APP%" set "APP=%ProgramFiles%\FullProject Studio\FullProjectStudio.exe"
if not exist "%APP%" (
  echo Non trovo FullProject Studio. E' installato su questo PC?
  pause
  exit /b 1
)

echo.
echo Esporto tutti i dati del gestionale in:
echo   %DEST%\fullprojectstudio-dati.zip
echo.
echo Attendi qualche secondo...
start "" /wait "%APP%" "--esporta-migrazione=%DEST%"
set "CODICE=%ERRORLEVEL%"

echo.
if exist "%DEST%\esito.txt" type "%DEST%\esito.txt"
echo.
if not "%CODICE%"=="0" (
  echo L'esportazione NON e' riuscita. Il file precedente, se c'era, non e' stato toccato.
  if /i not "%~1"=="/silenzioso" pause
  exit /b 1
)

rem Con /silenzioso (es. da Utilita' di pianificazione) non apre la cartella e non aspetta.
if /i "%~1"=="/silenzioso" exit /b 0
explorer "%DEST%"
pause
exit /b 0
