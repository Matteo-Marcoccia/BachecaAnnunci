USE BachecaAnnunci;

DELIMITER $$

-- OP1 e RV8: registrazione di un utente ordinario.
DROP PROCEDURE IF EXISTS sp_RegistraUtente $$
CREATE PROCEDURE sp_RegistraUtente (
    IN p_Username VARCHAR(30),
    IN p_PasswordHash VARCHAR(255),
    IN p_Nome VARCHAR(50),
    IN p_Cognome VARCHAR(50),
    IN p_CodiceFiscale CHAR(16),
    IN p_DataNascita DATE,
    IN p_IndirizzoResidenza VARCHAR(100),
    IN p_IndirizzoFatturazione VARCHAR(100),
    IN p_Email VARCHAR(100),
    IN p_Cellulare VARCHAR(20),
    IN p_TelefonoFisso VARCHAR(20),
    IN p_RecapitoPreferito VARCHAR(15)
)
SQL SECURITY DEFINER
BEGIN
    INSERT INTO Utente (
        Username,
        PasswordHash,
        Nome,
        Cognome,
        CodiceFiscale,
        DataNascita,
        IndirizzoResidenza,
        IndirizzoFatturazione,
        Email,
        Cellulare,
        TelefonoFisso,
        RecapitoPreferito,
        IsGestore
    )
    VALUES (
        p_Username,
        p_PasswordHash,
        p_Nome,
        p_Cognome,
        p_CodiceFiscale,
        p_DataNascita,
        p_IndirizzoResidenza,
        p_IndirizzoFatturazione,
        p_Email,
        p_Cellulare,
        p_TelefonoFisso,
        p_RecapitoPreferito,
        FALSE
    );
END $$

-- Procedura di supporto: elenco delle categorie selezionabili.
DROP PROCEDURE IF EXISTS sp_ElencoCategorie $$
CREATE PROCEDURE sp_ElencoCategorie ()
SQL SECURITY DEFINER
BEGIN
    SELECT
        CodiceCategoria,
        Nome,
        CategoriaPadre,
        GestoreCreatore
    FROM Categoria
    ORDER BY Nome;
END $$

-- OP2: pubblicazione di un nuovo annuncio.
DROP PROCEDURE IF EXISTS sp_PubblicaAnnuncio $$
CREATE PROCEDURE sp_PubblicaAnnuncio (
    IN p_Titolo VARCHAR(100),
    IN p_DescrizioneArticolo TEXT,
    IN p_Prezzo DECIMAL(10, 2),
    IN p_Autore VARCHAR(30),
    IN p_Categoria INT UNSIGNED
)
SQL SECURITY DEFINER
BEGIN
    INSERT INTO Annuncio (
        Titolo,
        DescrizioneArticolo,
        Prezzo,
        Stato,
        Autore,
        Categoria
    )
    VALUES (
        p_Titolo,
        p_DescrizioneArticolo,
        p_Prezzo,
        'InVendita',
        p_Autore,
        p_Categoria
    );
END $$

-- OP3: pubblicazione di una nota e recupero dei follower da notificare.
DROP PROCEDURE IF EXISTS sp_PubblicaNota $$
CREATE PROCEDURE sp_PubblicaNota (
    IN p_UsernameAutore VARCHAR(30),
    IN p_CodiceAnnuncio INT UNSIGNED,
    IN p_TestoNota TEXT
)
SQL SECURITY DEFINER
BEGIN
    DECLARE v_CodiceAnnuncio INT UNSIGNED DEFAULT NULL;

    DECLARE EXIT HANDLER FOR SQLEXCEPTION
    BEGIN
        ROLLBACK;
        RESIGNAL;
    END;

    SET TRANSACTION ISOLATION LEVEL READ COMMITTED;
    START TRANSACTION;

    SELECT Codice
    INTO v_CodiceAnnuncio
    FROM Annuncio
    WHERE Codice = p_CodiceAnnuncio
      AND Autore = p_UsernameAutore
      AND Stato = 'InVendita'
    FOR UPDATE;

    IF v_CodiceAnnuncio IS NULL THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'RV11: solo l autore puo integrare un proprio annuncio in vendita';
    END IF;

    INSERT INTO Nuova_Nota (
        TestoNota,
        DataOra,
        Annuncio
    )
    VALUES (
        p_TestoNota,
        CURRENT_TIMESTAMP,
        v_CodiceAnnuncio
    );

    COMMIT;

    SELECT
        r.Username,
        r.RecapitoPreferito,
        r.Recapito
    FROM Segui AS s
    JOIN v_RecapitiPreferiti AS r
      ON r.Username = s.UsernameUtente
    WHERE s.CodiceAnnuncio = p_CodiceAnnuncio;
END $$

-- OP4: recupero degli annunci di una categoria e delle sue sottocategorie.
DROP PROCEDURE IF EXISTS sp_RicercaAnnunciPerCategoria $$
CREATE PROCEDURE sp_RicercaAnnunciPerCategoria (
    IN p_CodiceCategoria INT UNSIGNED,
    IN p_UsernameRichiedente VARCHAR(30)
)
SQL SECURITY DEFINER
BEGIN
    WITH RECURSIVE SottoCategorie AS (
        SELECT CodiceCategoria
        FROM Categoria
        WHERE CodiceCategoria = p_CodiceCategoria

        UNION ALL

        SELECT c.CodiceCategoria
        FROM Categoria AS c
        JOIN SottoCategorie AS sc
          ON c.CategoriaPadre = sc.CodiceCategoria
    )
    SELECT
        a.Codice,
        a.Titolo,
        a.Prezzo,
        a.Stato,
        a.Autore,
        a.Categoria,
        c.Nome AS NomeCategoria
    FROM Annuncio AS a
    JOIN SottoCategorie AS sc
      ON sc.CodiceCategoria = a.Categoria
    JOIN Categoria AS c
      ON c.CodiceCategoria = a.Categoria
    WHERE a.Stato = 'InVendita'
      AND a.Autore <> p_UsernameRichiedente
    ORDER BY a.Titolo;
END $$

-- OP5: visualizzazione della scheda completa di un annuncio.
DROP PROCEDURE IF EXISTS sp_DettaglioAnnuncio $$
CREATE PROCEDURE sp_DettaglioAnnuncio (
    IN p_CodiceAnnuncio INT UNSIGNED
)
SQL SECURITY DEFINER
BEGIN
    SELECT *
    FROM v_DettaglioAnnuncio
    WHERE Codice = p_CodiceAnnuncio;
END $$

-- OP5: elenco delle note integrative di un annuncio.
DROP PROCEDURE IF EXISTS sp_ElencoNoteAnnuncio $$
CREATE PROCEDURE sp_ElencoNoteAnnuncio (
    IN p_CodiceAnnuncio INT UNSIGNED
)
SQL SECURITY DEFINER
BEGIN
    SELECT
        CodiceNota,
        TestoNota,
        DataOra
    FROM Nuova_Nota
    WHERE Annuncio = p_CodiceAnnuncio
    ORDER BY DataOra;
END $$

-- OP5: elenco dei commenti pubblici di un annuncio.
DROP PROCEDURE IF EXISTS sp_ElencoCommentiAnnuncio $$
CREATE PROCEDURE sp_ElencoCommentiAnnuncio (
    IN p_CodiceAnnuncio INT UNSIGNED
)
SQL SECURITY DEFINER
BEGIN
    SELECT
        CodiceCommento,
        Testo,
        DataOra,
        Autore
    FROM Commento_Pubblico
    WHERE Annuncio = p_CodiceAnnuncio
    ORDER BY DataOra;
END $$

-- OP6: inserimento di un commento pubblico.
DROP PROCEDURE IF EXISTS sp_InserisciCommento $$
CREATE PROCEDURE sp_InserisciCommento (
    IN p_Testo TEXT,
    IN p_CodiceAnnuncio INT UNSIGNED,
    IN p_Autore VARCHAR(30)
)
SQL SECURITY DEFINER
BEGIN
    INSERT INTO Commento_Pubblico (
        Testo,
        DataOra,
        Annuncio,
        Autore
    )
    VALUES (
        p_Testo,
        CURRENT_TIMESTAMP,
        p_CodiceAnnuncio,
        p_Autore
    );

    SELECT
        r.Username,
        r.RecapitoPreferito,
        r.Recapito
    FROM v_RecapitiPreferiti AS r
    JOIN (
        SELECT s.UsernameUtente AS Username
        FROM Segui AS s
        WHERE s.CodiceAnnuncio = p_CodiceAnnuncio
          AND s.UsernameUtente <> p_Autore

        UNION

        SELECT a.Autore AS Username
        FROM Annuncio AS a
        WHERE a.Codice = p_CodiceAnnuncio
          AND a.Autore <> p_Autore
    ) AS destinatari
      ON destinatari.Username = r.Username;
END $$

-- OP7: invio di un messaggio privato relativo a un annuncio.
DROP PROCEDURE IF EXISTS sp_InviaMessaggioPrivato $$
CREATE PROCEDURE sp_InviaMessaggioPrivato (
    IN p_Testo TEXT,
    IN p_CodiceAnnuncio INT UNSIGNED,
    IN p_Mittente VARCHAR(30),
    IN p_Destinatario VARCHAR(30)
)
SQL SECURITY DEFINER
BEGIN
    INSERT INTO Messaggio_Privato (
        Testo,
        DataOra,
        Annuncio,
        Mittente,
        Destinatario
    )
    VALUES (
        p_Testo,
        CURRENT_TIMESTAMP,
        p_CodiceAnnuncio,
        p_Mittente,
        p_Destinatario
    );

    SELECT
        Username,
        RecapitoPreferito,
        Recapito
    FROM v_RecapitiPreferiti
    WHERE Username = p_Destinatario;
END $$

-- Procedura di supporto a OP8: annunci gia seguiti dall'utente.
DROP PROCEDURE IF EXISTS sp_ElencoAnnunciSeguiti $$
CREATE PROCEDURE sp_ElencoAnnunciSeguiti (
    IN p_Username VARCHAR(30)
)
SQL SECURITY DEFINER
BEGIN
    SELECT
        d.Codice,
        d.Titolo,
        d.DescrizioneArticolo,
        d.Prezzo,
        d.Stato,
        d.Autore,
        d.CodiceCategoria,
        d.NomeCategoria
    FROM Segui AS s
    JOIN v_DettaglioAnnuncio AS d
      ON d.Codice = s.CodiceAnnuncio
    WHERE s.UsernameUtente = p_Username
    ORDER BY d.Titolo;
END $$

-- OP8: iscrizione al monitoraggio di un annuncio.
DROP PROCEDURE IF EXISTS sp_SeguiAnnuncio $$
CREATE PROCEDURE sp_SeguiAnnuncio (
    IN p_Username VARCHAR(30),
    IN p_CodiceAnnuncio INT UNSIGNED
)
SQL SECURITY DEFINER
BEGIN
    DECLARE v_CodiceAnnuncio INT UNSIGNED DEFAULT NULL;

    DECLARE EXIT HANDLER FOR SQLEXCEPTION
    BEGIN
        ROLLBACK;
        RESIGNAL;
    END;

    SET TRANSACTION ISOLATION LEVEL READ COMMITTED;
    START TRANSACTION;

    SELECT Codice
    INTO v_CodiceAnnuncio
    FROM Annuncio
    WHERE Codice = p_CodiceAnnuncio
      AND Stato = 'InVendita'
    FOR UPDATE;

    IF v_CodiceAnnuncio IS NULL THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'RV13: si possono seguire solo annunci in vendita';
    END IF;

    INSERT INTO Segui (
        UsernameUtente,
        CodiceAnnuncio
    )
    VALUES (
        p_Username,
        v_CodiceAnnuncio
    );

    COMMIT;
END $$

-- OP8: disiscrizione dal monitoraggio di un annuncio.
DROP PROCEDURE IF EXISTS sp_SmettiDiSeguireAnnuncio $$
CREATE PROCEDURE sp_SmettiDiSeguireAnnuncio (
    IN p_Username VARCHAR(30),
    IN p_CodiceAnnuncio INT UNSIGNED
)
SQL SECURITY DEFINER
BEGIN
    DELETE FROM Segui
    WHERE UsernameUtente = p_Username
      AND CodiceAnnuncio = p_CodiceAnnuncio;
END $$

-- OP9: modifica delle informazioni di un proprio annuncio attivo.
DROP PROCEDURE IF EXISTS sp_ModificaAnnuncio $$
CREATE PROCEDURE sp_ModificaAnnuncio (
    IN p_UsernameAutore VARCHAR(30),
    IN p_CodiceAnnuncio INT UNSIGNED,
    IN p_Titolo VARCHAR(100),
    IN p_DescrizioneArticolo TEXT,
    IN p_Prezzo DECIMAL(10, 2),
    IN p_Categoria INT UNSIGNED
)
SQL SECURITY DEFINER
BEGIN
    DECLARE v_CodiceAnnuncio INT UNSIGNED DEFAULT NULL;

    DECLARE EXIT HANDLER FOR SQLEXCEPTION
    BEGIN
        ROLLBACK;
        RESIGNAL;
    END;

    SET TRANSACTION ISOLATION LEVEL READ COMMITTED;
    START TRANSACTION;

    SELECT Codice
    INTO v_CodiceAnnuncio
    FROM Annuncio
    WHERE Codice = p_CodiceAnnuncio
      AND Autore = p_UsernameAutore
      AND Stato = 'InVendita'
    FOR UPDATE;

    IF v_CodiceAnnuncio IS NULL THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'RV11: solo l autore puo modificare un proprio annuncio in vendita';
    END IF;

    UPDATE Annuncio
    SET Titolo = p_Titolo,
        DescrizioneArticolo = p_DescrizioneArticolo,
        Prezzo = p_Prezzo,
        Categoria = p_Categoria
    WHERE Codice = v_CodiceAnnuncio;

    COMMIT;

    SELECT
        r.Username,
        r.RecapitoPreferito,
        r.Recapito
    FROM Segui AS s
    JOIN v_RecapitiPreferiti AS r
      ON r.Username = s.UsernameUtente
    WHERE s.CodiceAnnuncio = p_CodiceAnnuncio;
END $$

-- OP10: aggiornamento di un proprio annuncio allo stato Venduto.
DROP PROCEDURE IF EXISTS sp_ContrassegnaAnnuncioVenduto $$
CREATE PROCEDURE sp_ContrassegnaAnnuncioVenduto (
    IN p_UsernameAutore VARCHAR(30),
    IN p_CodiceAnnuncio INT UNSIGNED
)
SQL SECURITY DEFINER
BEGIN
    DECLARE v_CodiceAnnuncio INT UNSIGNED DEFAULT NULL;

    DECLARE EXIT HANDLER FOR SQLEXCEPTION
    BEGIN
        ROLLBACK;
        RESIGNAL;
    END;

    SET TRANSACTION ISOLATION LEVEL READ COMMITTED;
    START TRANSACTION;

    SELECT Codice
    INTO v_CodiceAnnuncio
    FROM Annuncio
    WHERE Codice = p_CodiceAnnuncio
      AND Autore = p_UsernameAutore
      AND Stato = 'InVendita'
    FOR UPDATE;

    IF v_CodiceAnnuncio IS NULL THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'RV11: solo l autore puo vendere un proprio annuncio in vendita';
    END IF;

    UPDATE Annuncio
    SET Stato = 'Venduto'
    WHERE Codice = v_CodiceAnnuncio;

    COMMIT;

    SELECT
        r.Username,
        r.RecapitoPreferito,
        r.Recapito
    FROM Segui AS s
    JOIN v_RecapitiPreferiti AS r
      ON r.Username = s.UsernameUtente
    WHERE s.CodiceAnnuncio = p_CodiceAnnuncio;
END $$

-- OP11: creazione e collocazione di una categoria.
DROP PROCEDURE IF EXISTS sp_CreaCategoria $$
CREATE PROCEDURE sp_CreaCategoria (
    IN p_Nome VARCHAR(50),
    IN p_CategoriaPadre INT UNSIGNED,
    IN p_GestoreCreatore VARCHAR(30)
)
SQL SECURITY DEFINER
BEGIN
    INSERT INTO Categoria (
        Nome,
        CategoriaPadre,
        GestoreCreatore
    )
    VALUES (
        p_Nome,
        p_CategoriaPadre,
        p_GestoreCreatore
    );
END $$

-- OP12: generazione del report statistico riservato ai gestori.
DROP PROCEDURE IF EXISTS sp_GeneraReportUtenti $$
CREATE PROCEDURE sp_GeneraReportUtenti (
    IN p_UsernameGestore VARCHAR(30)
)
SQL SECURITY DEFINER
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM Utente
        WHERE Username = p_UsernameGestore
          AND IsGestore = TRUE
    ) THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'RV12: il report e riservato ai gestori';
    END IF;

    SELECT
        Username,
        AnnunciTotali,
        AnnunciVenduti,
        PercentualeVenduti
    FROM v_StatisticheUtenti
    ORDER BY Username;
END $$

-- OP13: recupero dei dati necessari all'autenticazione e alla sessione.
DROP PROCEDURE IF EXISTS sp_RecuperaProfiloAccesso $$
CREATE PROCEDURE sp_RecuperaProfiloAccesso (
    IN p_Username VARCHAR(30)
)
SQL SECURITY DEFINER
BEGIN
    SELECT
        Username,
        PasswordHash,
        Nome,
        Cognome,
        IsGestore
    FROM Utente
    WHERE Username = p_Username;
END $$

-- OP14: visualizzazione degli annunci pubblicati da un utente.
DROP PROCEDURE IF EXISTS sp_ElencoAnnunciUtente $$
CREATE PROCEDURE sp_ElencoAnnunciUtente (
    IN p_Username VARCHAR(30)
)
SQL SECURITY DEFINER
BEGIN
    SELECT
        Codice,
        Titolo,
        DescrizioneArticolo,
        Prezzo,
        Stato,
        CodiceCategoria,
        NomeCategoria
    FROM v_DettaglioAnnuncio
    WHERE Autore = p_Username
    ORDER BY Titolo;
END $$

-- OP15: visualizzazione della conversazione privata relativa a un annuncio.
DROP PROCEDURE IF EXISTS sp_ElencoConversazioniUtente $$
CREATE PROCEDURE sp_ElencoConversazioniUtente (
    IN p_Username VARCHAR(30)
)
SQL SECURITY DEFINER
BEGIN
    SELECT DISTINCT
        m.Annuncio AS CodiceAnnuncio,
        a.Titolo AS TitoloAnnuncio,
        CASE
            WHEN m.Mittente = p_Username THEN m.Destinatario
            ELSE m.Mittente
        END AS AltroUtente
    FROM Messaggio_Privato AS m
    JOIN Annuncio AS a
      ON a.Codice = m.Annuncio
    WHERE m.Mittente = p_Username
       OR m.Destinatario = p_Username
    ORDER BY TitoloAnnuncio, AltroUtente;
END $$

-- OP15: visualizzazione della conversazione privata relativa a un annuncio.
DROP PROCEDURE IF EXISTS sp_VisualizzaConversazione $$
CREATE PROCEDURE sp_VisualizzaConversazione (
    IN p_UsernameRichiedente VARCHAR(30),
    IN p_AltroUtente VARCHAR(30),
    IN p_CodiceAnnuncio INT UNSIGNED
)
SQL SECURITY DEFINER
BEGIN
    IF p_UsernameRichiedente = p_AltroUtente THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'RV4: mittente e destinatario devono essere distinti';
    END IF;

    IF NOT EXISTS (
        SELECT 1
        FROM Annuncio
        WHERE Codice = p_CodiceAnnuncio
          AND Autore IN (p_UsernameRichiedente, p_AltroUtente)
    ) THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'RV6: uno dei partecipanti deve essere autore dell annuncio';
    END IF;

    SELECT
        CodiceMessaggio,
        Testo,
        DataOra,
        Mittente,
        Destinatario
    FROM Messaggio_Privato
    WHERE Annuncio = p_CodiceAnnuncio
      AND (
          (Mittente = p_UsernameRichiedente AND Destinatario = p_AltroUtente)
          OR
          (Mittente = p_AltroUtente AND Destinatario = p_UsernameRichiedente)
      )
    ORDER BY DataOra, CodiceMessaggio;
END $$

-- Controllo interno: coerenza tra account DBMS e profilo applicativo.
DROP PROCEDURE IF EXISTS sp_VerificaProfiloSessione $$
CREATE PROCEDURE sp_VerificaProfiloSessione (
    IN p_Username VARCHAR(30)
)
SQL SECURITY DEFINER
BEGIN
    DECLARE v_IsGestore BOOLEAN DEFAULT NULL;
    DECLARE v_AccountDBMS VARCHAR(100);

    SELECT IsGestore
    INTO v_IsGestore
    FROM Utente
    WHERE Username = p_Username;

    IF v_IsGestore IS NULL THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Sessione non valida: utente inesistente';
    END IF;

    SET v_AccountDBMS = SUBSTRING_INDEX(USER(), '@', 1);

    IF NOT (
        (v_AccountDBMS = 'account_utente' AND v_IsGestore = FALSE)
        OR
        (v_AccountDBMS = 'account_gestore' AND v_IsGestore = TRUE)
    ) THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Sessione non valida: profilo e ruolo DBMS non coerenti';
    END IF;

    SELECT
        p_Username AS Username,
        IF(v_IsGestore, 'Gestore', 'Utente') AS ProfiloVerificato;
END $$

DELIMITER ;
