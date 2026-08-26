USE BachecaAnnunci;

-- Profili applicativi rappresentati come ruoli del DBMS.
CREATE ROLE IF NOT EXISTS 'ruolo_accesso';
CREATE ROLE IF NOT EXISTS 'ruolo_utente';
CREATE ROLE IF NOT EXISTS 'ruolo_gestore';

-- Account tecnici utilizzati dal thin client.
-- Le password devono essere sostituite durante la configurazione.
CREATE USER IF NOT EXISTS 'account_accesso'@'localhost'
    IDENTIFIED BY 'Accesso_DB_2026!';

CREATE USER IF NOT EXISTS 'account_utente'@'localhost'
    IDENTIFIED BY 'Utente_DB_2026!';

CREATE USER IF NOT EXISTS 'account_gestore'@'localhost'
    IDENTIFIED BY 'Gestore_DB_2026!';

-- Associazione degli account tecnici ai rispettivi ruoli.
-- Gli account e le relative password sono configurati separatamente.
GRANT 'ruolo_accesso'
TO 'account_accesso'@'localhost';

GRANT 'ruolo_utente'
TO 'account_utente'@'localhost';

GRANT 'ruolo_gestore'
TO 'account_gestore'@'localhost';

-- Attivazione automatica del ruolo all'apertura della connessione.
SET DEFAULT ROLE 'ruolo_accesso'
TO 'account_accesso'@'localhost';

SET DEFAULT ROLE 'ruolo_utente'
TO 'account_utente'@'localhost';

SET DEFAULT ROLE 'ruolo_gestore'
TO 'account_gestore'@'localhost';

-- Registrazione e recupero del profilo di accesso: OP1 e OP13.
GRANT EXECUTE ON PROCEDURE BachecaAnnunci.sp_RegistraUtente
TO 'ruolo_accesso';

GRANT EXECUTE ON PROCEDURE BachecaAnnunci.sp_RecuperaProfiloAccesso
TO 'ruolo_accesso';

-- Operazioni ordinarie dell'utente registrato: OP2-OP10, OP14 e OP15.
GRANT EXECUTE ON PROCEDURE BachecaAnnunci.sp_ElencoCategorie
TO 'ruolo_utente';

GRANT EXECUTE ON PROCEDURE BachecaAnnunci.sp_PubblicaAnnuncio
TO 'ruolo_utente';

GRANT EXECUTE ON PROCEDURE BachecaAnnunci.sp_PubblicaNota
TO 'ruolo_utente';

GRANT EXECUTE ON PROCEDURE BachecaAnnunci.sp_RicercaAnnunciPerCategoria
TO 'ruolo_utente';

GRANT EXECUTE ON PROCEDURE BachecaAnnunci.sp_DettaglioAnnuncio
TO 'ruolo_utente';

GRANT EXECUTE ON PROCEDURE BachecaAnnunci.sp_ElencoNoteAnnuncio
TO 'ruolo_utente';

GRANT EXECUTE ON PROCEDURE BachecaAnnunci.sp_ElencoCommentiAnnuncio
TO 'ruolo_utente';

GRANT EXECUTE ON PROCEDURE BachecaAnnunci.sp_InserisciCommento
TO 'ruolo_utente';

GRANT EXECUTE ON PROCEDURE BachecaAnnunci.sp_InviaMessaggioPrivato
TO 'ruolo_utente';

GRANT EXECUTE ON PROCEDURE BachecaAnnunci.sp_SeguiAnnuncio
TO 'ruolo_utente';

GRANT EXECUTE ON PROCEDURE BachecaAnnunci.sp_ElencoAnnunciSeguiti
TO 'ruolo_utente';

GRANT EXECUTE ON PROCEDURE BachecaAnnunci.sp_SmettiDiSeguireAnnuncio
TO 'ruolo_utente';

GRANT EXECUTE ON PROCEDURE BachecaAnnunci.sp_ModificaAnnuncio
TO 'ruolo_utente';

GRANT EXECUTE ON PROCEDURE BachecaAnnunci.sp_ContrassegnaAnnuncioVenduto
TO 'ruolo_utente';

GRANT EXECUTE ON PROCEDURE BachecaAnnunci.sp_ElencoAnnunciUtente
TO 'ruolo_utente';

GRANT EXECUTE ON PROCEDURE BachecaAnnunci.sp_VisualizzaConversazione
TO 'ruolo_utente';

GRANT EXECUTE ON PROCEDURE BachecaAnnunci.sp_ElencoConversazioniUtente
TO 'ruolo_utente';

GRANT EXECUTE ON PROCEDURE BachecaAnnunci.sp_VerificaProfiloSessione
TO 'ruolo_utente';

-- Il gestore eredita tutte le operazioni ordinarie.
GRANT 'ruolo_utente'
TO 'ruolo_gestore';

-- Operazioni riservate al gestore: OP11 e OP12.
GRANT EXECUTE ON PROCEDURE BachecaAnnunci.sp_CreaCategoria
TO 'ruolo_gestore';

GRANT EXECUTE ON PROCEDURE BachecaAnnunci.sp_GeneraReportUtenti
TO 'ruolo_gestore';