USE BachecaAnnunci;

-- Abilitazione dello scheduler durante la configurazione del sistema.
SET GLOBAL event_scheduler = ON;

DROP EVENT IF EXISTS ev_EliminaMessaggiObsoleti;

-- Elimina mensilmente i messaggi privati risalenti a piu di tre anni prima.
CREATE EVENT ev_EliminaMessaggiObsoleti
ON SCHEDULE EVERY 1 MONTH
STARTS CURRENT_TIMESTAMP + INTERVAL 1 MONTH
ON COMPLETION PRESERVE
ENABLE
DO
    DELETE FROM Messaggio_Privato
    WHERE DataOra < CURRENT_TIMESTAMP - INTERVAL 3 YEAR;
