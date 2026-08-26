USE BachecaAnnunci;

-- Dati necessari per indirizzare le notifiche simulate.
CREATE OR REPLACE VIEW v_RecapitiPreferiti AS
SELECT
    Username,
    RecapitoPreferito,
    CASE RecapitoPreferito
        WHEN 'Email' THEN Email
        WHEN 'Cellulare' THEN Cellulare
        WHEN 'TelefonoFisso' THEN TelefonoFisso
    END AS Recapito
FROM Utente;

-- Informazioni principali mostrate nella scheda di un annuncio.
CREATE OR REPLACE VIEW v_DettaglioAnnuncio AS
SELECT
    a.Codice,
    a.Titolo,
    a.DescrizioneArticolo,
    a.Prezzo,
    a.Stato,
    a.Autore,
    c.CodiceCategoria,
    c.Nome AS NomeCategoria,
    u.Nome AS NomeAutore,
    u.Cognome AS CognomeAutore,
    u.RecapitoPreferito,
    CASE u.RecapitoPreferito
        WHEN 'Email' THEN u.Email
        WHEN 'Cellulare' THEN u.Cellulare
        WHEN 'TelefonoFisso' THEN u.TelefonoFisso
    END AS RecapitoAutore
FROM Annuncio AS a
JOIN Categoria AS c
  ON c.CodiceCategoria = a.Categoria
JOIN Utente AS u
  ON u.Username = a.Autore;

-- Statistiche correnti utilizzate dal report riservato ai gestori.
CREATE OR REPLACE VIEW v_StatisticheUtenti AS
SELECT
    u.Username,
    COUNT(a.Codice) AS AnnunciTotali,
    COALESCE(SUM(a.Stato = 'Venduto'), 0) AS AnnunciVenduti,
    ROUND(
        COALESCE(
            100.0 * SUM(a.Stato = 'Venduto') / NULLIF(COUNT(a.Codice), 0),
            0
        ),
        2
    ) AS PercentualeVenduti
FROM Utente AS u
LEFT JOIN Annuncio AS a
  ON a.Autore = u.Username
GROUP BY u.Username;
