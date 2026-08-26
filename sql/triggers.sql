USE BachecaAnnunci;

DELIMITER $$

DROP TRIGGER IF EXISTS trg_Utente_BI_recapiti $$
CREATE TRIGGER trg_Utente_BI_recapiti
BEFORE INSERT ON Utente
FOR EACH ROW
BEGIN
    IF NULLIF(TRIM(NEW.Nome), '') IS NULL
       OR NULLIF(TRIM(NEW.Cognome), '') IS NULL
       OR NULLIF(TRIM(NEW.IndirizzoResidenza), '') IS NULL THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'RV19: i campi testuali obbligatori non possono essere vuoti';
    END IF;

    IF NOT REGEXP_LIKE(NEW.CodiceFiscale, '^[A-Z0-9]{16}$', 'c') THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'RV14: codice fiscale non valido';
    END IF;

    IF NEW.Email IS NOT NULL
       AND NOT REGEXP_LIKE(NEW.Email, '^[^[:space:]@]+@[^[:space:]@]+[.][^[:space:]@]+$', 'c') THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'RV15: formato email non valido';
    END IF;

    IF (NEW.Cellulare IS NOT NULL
        AND NOT REGEXP_LIKE(NEW.Cellulare, '^[+]?[0-9]{7,15}$', 'c'))
       OR (NEW.TelefonoFisso IS NOT NULL
        AND NOT REGEXP_LIKE(NEW.TelefonoFisso, '^[+]?[0-9]{7,15}$', 'c')) THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'RV16: formato telefonico non valido';
    END IF;

    IF NEW.DataNascita > CURRENT_DATE() THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'RV17: la data di nascita non puo essere futura';
    END IF;

    IF NULLIF(TRIM(NEW.Email), '') IS NULL
       AND NULLIF(TRIM(NEW.Cellulare), '') IS NULL
       AND NULLIF(TRIM(NEW.TelefonoFisso), '') IS NULL THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'RV1: inserire almeno un recapito';
    END IF;

    IF NEW.RecapitoPreferito NOT IN ('Email', 'Cellulare', 'TelefonoFisso')
       OR (NEW.RecapitoPreferito = 'Email'
           AND NULLIF(TRIM(NEW.Email), '') IS NULL)
       OR (NEW.RecapitoPreferito = 'Cellulare'
           AND NULLIF(TRIM(NEW.Cellulare), '') IS NULL)
       OR (NEW.RecapitoPreferito = 'TelefonoFisso'
           AND NULLIF(TRIM(NEW.TelefonoFisso), '') IS NULL) THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'RV2: il recapito preferito deve essere valorizzato';
    END IF;
END $$

DROP TRIGGER IF EXISTS trg_Utente_BU_recapiti $$
CREATE TRIGGER trg_Utente_BU_recapiti
BEFORE UPDATE ON Utente
FOR EACH ROW
BEGIN
    IF NULLIF(TRIM(NEW.Nome), '') IS NULL
       OR NULLIF(TRIM(NEW.Cognome), '') IS NULL
       OR NULLIF(TRIM(NEW.IndirizzoResidenza), '') IS NULL THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'RV19: i campi testuali obbligatori non possono essere vuoti';
    END IF;

    IF NOT REGEXP_LIKE(NEW.CodiceFiscale, '^[A-Z0-9]{16}$', 'c') THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'RV14: codice fiscale non valido';
    END IF;

    IF NEW.Email IS NOT NULL
       AND NOT REGEXP_LIKE(NEW.Email, '^[^[:space:]@]+@[^[:space:]@]+[.][^[:space:]@]+$', 'c') THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'RV15: formato email non valido';
    END IF;

    IF (NEW.Cellulare IS NOT NULL
        AND NOT REGEXP_LIKE(NEW.Cellulare, '^[+]?[0-9]{7,15}$', 'c'))
       OR (NEW.TelefonoFisso IS NOT NULL
        AND NOT REGEXP_LIKE(NEW.TelefonoFisso, '^[+]?[0-9]{7,15}$', 'c')) THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'RV16: formato telefonico non valido';
    END IF;

    IF NEW.DataNascita > CURRENT_DATE() THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'RV17: la data di nascita non puo essere futura';
    END IF;

    IF NULLIF(TRIM(NEW.Email), '') IS NULL
       AND NULLIF(TRIM(NEW.Cellulare), '') IS NULL
       AND NULLIF(TRIM(NEW.TelefonoFisso), '') IS NULL THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'RV1: inserire almeno un recapito';
    END IF;

    IF NEW.RecapitoPreferito NOT IN ('Email', 'Cellulare', 'TelefonoFisso')
       OR (NEW.RecapitoPreferito = 'Email'
           AND NULLIF(TRIM(NEW.Email), '') IS NULL)
       OR (NEW.RecapitoPreferito = 'Cellulare'
           AND NULLIF(TRIM(NEW.Cellulare), '') IS NULL)
       OR (NEW.RecapitoPreferito = 'TelefonoFisso'
           AND NULLIF(TRIM(NEW.TelefonoFisso), '') IS NULL) THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'RV2: il recapito preferito deve essere valorizzato';
    END IF;
END $$

DROP TRIGGER IF EXISTS trg_Categoria_BI_vincoli $$
CREATE TRIGGER trg_Categoria_BI_vincoli
BEFORE INSERT ON Categoria
FOR EACH ROW
BEGIN
    IF NULLIF(TRIM(NEW.Nome), '') IS NULL THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'RV19: il nome della categoria non puo essere vuoto';
    END IF;

    IF NOT EXISTS (
        SELECT 1
        FROM Utente
        WHERE Username = NEW.GestoreCreatore
          AND IsGestore = TRUE
    ) THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'RV5: il creatore della categoria deve essere un gestore';
    END IF;

    IF EXISTS (
        SELECT 1
        FROM Categoria
        WHERE TRIM(Nome) = TRIM(NEW.Nome)
          AND CategoriaPadre <=> NEW.CategoriaPadre
    ) THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'RV9: categoria gia presente con quel padre';
    END IF;
END $$

DROP TRIGGER IF EXISTS trg_Categoria_BU_vincoli $$
CREATE TRIGGER trg_Categoria_BU_vincoli
BEFORE UPDATE ON Categoria
FOR EACH ROW
BEGIN
    DECLARE v_antenato INT UNSIGNED;

    IF NULLIF(TRIM(NEW.Nome), '') IS NULL THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'RV19: il nome della categoria non puo essere vuoto';
    END IF;

    IF NOT EXISTS (
        SELECT 1
        FROM Utente
        WHERE Username = NEW.GestoreCreatore
          AND IsGestore = TRUE
    ) THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'RV5: il creatore della categoria deve essere un gestore';
    END IF;

    IF EXISTS (
        SELECT 1
        FROM Categoria
        WHERE TRIM(Nome) = TRIM(NEW.Nome)
          AND CategoriaPadre <=> NEW.CategoriaPadre
          AND CodiceCategoria <> OLD.CodiceCategoria
    ) THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'RV9: categoria gia presente con quel padre';
    END IF;

    SET v_antenato = NEW.CategoriaPadre;

    WHILE v_antenato IS NOT NULL DO
        IF v_antenato = NEW.CodiceCategoria THEN
            SIGNAL SQLSTATE '45000'
                SET MESSAGE_TEXT = 'RV3: la gerarchia delle categorie non puo contenere cicli';
        END IF;

        SET v_antenato = (
            SELECT CategoriaPadre
            FROM Categoria
            WHERE CodiceCategoria = v_antenato
        );
    END WHILE;
END $$

DROP TRIGGER IF EXISTS trg_Segui_BI_autore $$
CREATE TRIGGER trg_Segui_BI_autore
BEFORE INSERT ON Segui
FOR EACH ROW
BEGIN
    IF EXISTS (
        SELECT 1
        FROM Annuncio
        WHERE Codice = NEW.CodiceAnnuncio
          AND Autore = NEW.UsernameUtente
    ) THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'RV10: l autore non puo seguire il proprio annuncio';
    END IF;

    IF EXISTS (
        SELECT 1
        FROM Annuncio
        WHERE Codice = NEW.CodiceAnnuncio
          AND Stato <> 'InVendita'
    ) THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'RV13: si possono seguire solo annunci in vendita';
    END IF;
END $$

DROP TRIGGER IF EXISTS trg_Segui_BU_autore $$
CREATE TRIGGER trg_Segui_BU_autore
BEFORE UPDATE ON Segui
FOR EACH ROW
BEGIN
    IF EXISTS (
        SELECT 1
        FROM Annuncio
        WHERE Codice = NEW.CodiceAnnuncio
          AND Autore = NEW.UsernameUtente
    ) THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'RV10: l autore non puo seguire il proprio annuncio';
    END IF;

    IF EXISTS (
        SELECT 1
        FROM Annuncio
        WHERE Codice = NEW.CodiceAnnuncio
          AND Stato <> 'InVendita'
    ) THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'RV13: si possono seguire solo annunci in vendita';
    END IF;
END $$

DROP TRIGGER IF EXISTS trg_Annuncio_BI_stato $$
CREATE TRIGGER trg_Annuncio_BI_stato
BEFORE INSERT ON Annuncio
FOR EACH ROW
BEGIN
    IF NULLIF(TRIM(NEW.Titolo), '') IS NULL
       OR NULLIF(TRIM(NEW.DescrizioneArticolo), '') IS NULL THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'RV19: titolo e descrizione non possono essere vuoti';
    END IF;

    IF NEW.Prezzo <= 0 THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'RV18: il prezzo deve essere maggiore di zero';
    END IF;

    IF NEW.Stato NOT IN ('InVendita', 'Venduto') THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'RV7: stato dell annuncio non valido';
    END IF;
END $$

DROP TRIGGER IF EXISTS trg_Annuncio_BU_stato $$
CREATE TRIGGER trg_Annuncio_BU_stato
BEFORE UPDATE ON Annuncio
FOR EACH ROW
BEGIN
    IF NULLIF(TRIM(NEW.Titolo), '') IS NULL
       OR NULLIF(TRIM(NEW.DescrizioneArticolo), '') IS NULL THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'RV19: titolo e descrizione non possono essere vuoti';
    END IF;

    IF NEW.Prezzo <= 0 THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'RV18: il prezzo deve essere maggiore di zero';
    END IF;

    IF NEW.Stato NOT IN ('InVendita', 'Venduto') THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'RV7: stato dell annuncio non valido';
    END IF;
END $$

DROP TRIGGER IF EXISTS trg_Messaggio_BI_vincoli $$
CREATE TRIGGER trg_Messaggio_BI_vincoli
BEFORE INSERT ON Messaggio_Privato
FOR EACH ROW
BEGIN
    DECLARE v_autore VARCHAR(30);
    DECLARE v_stato VARCHAR(10);
    DECLARE v_conversazione_esistente INT DEFAULT 0;

    IF NULLIF(TRIM(NEW.Testo), '') IS NULL THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'RV19: il testo del messaggio non puo essere vuoto';
    END IF;

    IF NEW.Mittente = NEW.Destinatario THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'RV4: mittente e destinatario devono essere distinti';
    END IF;

    SELECT Autore, Stato
    INTO v_autore, v_stato
    FROM Annuncio
    WHERE Codice = NEW.Annuncio;

    IF v_stato <> 'InVendita' THEN
        SELECT COUNT(*)
        INTO v_conversazione_esistente
        FROM Messaggio_Privato
        WHERE Annuncio = NEW.Annuncio
          AND (
                (Mittente = NEW.Mittente
                 AND Destinatario = NEW.Destinatario)
             OR (Mittente = NEW.Destinatario
                 AND Destinatario = NEW.Mittente)
          );
    END IF;

    IF v_stato <> 'InVendita'
       AND v_conversazione_esistente = 0 THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'RV20: non si puo avviare una nuova conversazione per un annuncio venduto';
    END IF;

    IF v_autore IS NOT NULL
       AND NEW.Mittente <> v_autore
       AND NEW.Destinatario <> v_autore THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'RV6: uno dei partecipanti deve essere autore dell annuncio';
    END IF;
END $$

DROP TRIGGER IF EXISTS trg_Messaggio_BU_vincoli $$
CREATE TRIGGER trg_Messaggio_BU_vincoli
BEFORE UPDATE ON Messaggio_Privato
FOR EACH ROW
BEGIN
    DECLARE v_autore VARCHAR(30);

    IF NULLIF(TRIM(NEW.Testo), '') IS NULL THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'RV19: il testo del messaggio non puo essere vuoto';
    END IF;

    IF NEW.Mittente = NEW.Destinatario THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'RV4: mittente e destinatario devono essere distinti';
    END IF;

    SET v_autore = (
        SELECT Autore
        FROM Annuncio
        WHERE Codice = NEW.Annuncio
    );

    IF v_autore IS NOT NULL
       AND NEW.Mittente <> v_autore
       AND NEW.Destinatario <> v_autore THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'RV6: uno dei partecipanti deve essere autore dell annuncio';
    END IF;
END $$

DROP TRIGGER IF EXISTS trg_Nota_BI_testo $$
CREATE TRIGGER trg_Nota_BI_testo
BEFORE INSERT ON Nuova_Nota
FOR EACH ROW
BEGIN
    IF NULLIF(TRIM(NEW.TestoNota), '') IS NULL THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'RV19: il testo della nota non puo essere vuoto';
    END IF;
END $$

DROP TRIGGER IF EXISTS trg_Nota_BU_testo $$
CREATE TRIGGER trg_Nota_BU_testo
BEFORE UPDATE ON Nuova_Nota
FOR EACH ROW
BEGIN
    IF NULLIF(TRIM(NEW.TestoNota), '') IS NULL THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'RV19: il testo della nota non puo essere vuoto';
    END IF;
END $$

DROP TRIGGER IF EXISTS trg_Commento_BI_testo $$
CREATE TRIGGER trg_Commento_BI_testo
BEFORE INSERT ON Commento_Pubblico
FOR EACH ROW
BEGIN
    DECLARE v_stato VARCHAR(10);

    IF NULLIF(TRIM(NEW.Testo), '') IS NULL THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'RV19: il testo del commento non puo essere vuoto';
    END IF;

    SELECT Stato
    INTO v_stato
    FROM Annuncio
    WHERE Codice = NEW.Annuncio;

    IF v_stato <> 'InVendita' THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'RV20: non si puo interagire con un annuncio venduto';
    END IF;
END $$

DROP TRIGGER IF EXISTS trg_Commento_BU_testo $$
CREATE TRIGGER trg_Commento_BU_testo
BEFORE UPDATE ON Commento_Pubblico
FOR EACH ROW
BEGIN
    IF NULLIF(TRIM(NEW.Testo), '') IS NULL THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'RV19: il testo del commento non puo essere vuoto';
    END IF;
END $$

DELIMITER ;
