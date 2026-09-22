-- Fictitious data, loaded only when creating the Docker demo volume.
-- All three application passwords are Demo2026! (salted PBKDF2 hashes).
USE BachecaAnnunci;
INSERT INTO Utente
(Username, PasswordHash, Nome, Cognome, CodiceFiscale, DataNascita,
 IndirizzoResidenza, Email, RecapitoPreferito, IsGestore)
VALUES
('gestore_demo', 'pbkdf2_sha256$210000$mup3g6mW1DqnLQhklGZkjA==$tfl0UkEjB1wyICS9hkrNRBOLokQ+8D+MIqKeirEjVq8=', 'Demo', 'Utente', 'DEMO000000000001', '1990-01-01', 'Via Esempio 1', 'gestore_demo@example.com', 'Email', 1),
('venditore_demo', 'pbkdf2_sha256$210000$OZ5y0GS7rLL9aRpvjv8vGg==$6T9vclUt52bnpWAxVProKO793pwjOf8o5rjF5A8iYqI=', 'Demo', 'Utente', 'DEMO000000000002', '1990-01-01', 'Via Esempio 1', 'venditore_demo@example.com', 'Email', 0),
('utente_demo', 'pbkdf2_sha256$210000$pvrs9yVvFMZTrrfjhkt1mQ==$fAaUYXNagRw8ss3uofmaSC3bEC+UAAErOEgksCcbhsE=', 'Demo', 'Utente', 'DEMO000000000003', '1990-01-01', 'Via Esempio 1', 'utente_demo@example.com', 'Email', 0);

INSERT INTO Categoria (Nome, CategoriaPadre, GestoreCreatore)
VALUES ('Elettronica', NULL, 'gestore_demo');
SET @parent = LAST_INSERT_ID();
INSERT INTO Categoria (Nome, CategoriaPadre, GestoreCreatore)
VALUES ('Informatica', @parent, 'gestore_demo');
SET @child = LAST_INSERT_ID();
INSERT INTO Annuncio (Titolo, DescrizioneArticolo, Prezzo, Autore, Categoria)
VALUES ('Tastiera meccanica', 'Annuncio dimostrativo: tastiera usata in buone condizioni.',
        35.00, 'venditore_demo', @child);
