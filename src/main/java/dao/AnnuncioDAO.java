package dao;

import config.DatabaseConnection;
import model.Annuncio;
import model.DettaglioAnnuncio;
import model.DestinatarioNotifica;
import sessione.SessioneUtente;

import java.math.BigDecimal;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public final class AnnuncioDAO {

    private static final String PUBBLICA_ANNUNCIO =
            "{CALL sp_PubblicaAnnuncio(?, ?, ?, ?, ?)}";
    private static final String ELENCO_ANNUNCI_UTENTE =
            "{CALL sp_ElencoAnnunciUtente(?)}";
    private static final String MODIFICA_ANNUNCIO =
            "{CALL sp_ModificaAnnuncio(?, ?, ?, ?, ?, ?)}";
    private static final String RICERCA_PER_CATEGORIA =
            "{CALL sp_RicercaAnnunciPerCategoria(?, ?)}";
    private static final String DETTAGLIO_ANNUNCIO =
            "{CALL sp_DettaglioAnnuncio(?)}";
    private static final String CONTRASSEGNA_VENDUTO =
            "{CALL sp_ContrassegnaAnnuncioVenduto(?, ?)}";

    // OP2: pubblicazione di un annuncio.
    public void pubblica(
            SessioneUtente sessione,
            String titolo,
            String descrizione,
            BigDecimal prezzo,
            int codiceCategoria
    ) throws SQLException {
        try (Connection connection =
                     DatabaseConnection.openOperativa(sessione);
             CallableStatement statement =
                     connection.prepareCall(PUBBLICA_ANNUNCIO)) {

            statement.setString(1, titolo);
            statement.setString(2, descrizione);
            statement.setBigDecimal(3, prezzo);
            statement.setString(4, sessione.getUsername());
            statement.setInt(5, codiceCategoria);

            statement.executeUpdate();
        }
    }

    // OP4: ricerca degli annunci per categoria e sottocategorie.
    public List<Annuncio> ricercaPerCategoria(
            SessioneUtente sessione,
            int codiceCategoria
    ) throws SQLException {
        List<Annuncio> annunci = new ArrayList<>();

        try (Connection connection = DatabaseConnection.openOperativa(sessione);
            CallableStatement statement =
                     connection.prepareCall(RICERCA_PER_CATEGORIA)) {
            statement.setInt(1, codiceCategoria);
            statement.setString(2, sessione.getUsername());

            try (ResultSet result = statement.executeQuery()) {
                while (result.next()) {
                    annunci.add(new Annuncio(
                            result.getInt("Codice"),
                            result.getString("Titolo"),
                            null,
                            result.getBigDecimal("Prezzo"),
                            result.getString("Stato"),
                            result.getString("Autore"),
                            result.getInt("Categoria"),
                            result.getString("NomeCategoria")
                    ));
                }
            }
        }
        return annunci;
    }

    // OP5: lettura dei dati principali della scheda di dettaglio.
    public DettaglioAnnuncio trovaDettaglio(
            SessioneUtente sessione,
            int codiceAnnuncio
    ) throws SQLException {
        try (Connection connection = DatabaseConnection.openOperativa(sessione);
             CallableStatement statement =
                     connection.prepareCall(DETTAGLIO_ANNUNCIO)) {
            statement.setInt(1, codiceAnnuncio);
            try (ResultSet result = statement.executeQuery()) {
                if (!result.next()) {
                    throw new SQLException("Annuncio non trovato.");
                }
                return new DettaglioAnnuncio(
                        result.getInt("Codice"),
                        result.getString("Titolo"),
                        result.getString("DescrizioneArticolo"),
                        result.getBigDecimal("Prezzo"),
                        result.getString("Stato"),
                        result.getString("Autore"),
                        result.getInt("CodiceCategoria"),
                        result.getString("NomeCategoria"),
                        result.getString("NomeAutore"),
                        result.getString("CognomeAutore"),
                        result.getString("RecapitoPreferito"),
                        result.getString("RecapitoAutore")
                );
            }
        }
    }

    // OP9: modifica di un annuncio.
    public List<DestinatarioNotifica> modifica(
            SessioneUtente sessione,
            int codiceAnnuncio,
            String titolo,
            String descrizione,
            BigDecimal prezzo,
            int codiceCategoria
    ) throws SQLException {
        try (Connection connection =
                     DatabaseConnection.openOperativa(sessione);
             CallableStatement statement =
                     connection.prepareCall(MODIFICA_ANNUNCIO)) {

            statement.setString(1, sessione.getUsername());
            statement.setInt(2, codiceAnnuncio);
            statement.setString(3, titolo);
            statement.setString(4, descrizione);
            statement.setBigDecimal(5, prezzo);
            statement.setInt(6, codiceCategoria);

            List<DestinatarioNotifica> destinatari = new ArrayList<>();
            if (statement.execute()) {
                try (ResultSet result = statement.getResultSet()) {
                    while (result.next()) {
                        destinatari.add(new DestinatarioNotifica(
                                result.getString("Username"),
                                result.getString("RecapitoPreferito"),
                                result.getString("Recapito")
                        ));
                    }
                }
            }
            return destinatari;
        }
    }

    // OP10: aggiornamento dello stato a Venduto.
    public List<DestinatarioNotifica> contrassegnaVenduto(
            SessioneUtente sessione,
            int codiceAnnuncio
    ) throws SQLException {
        List<DestinatarioNotifica> destinatari = new ArrayList<>();
        try (Connection connection = DatabaseConnection.openOperativa(sessione);
             CallableStatement statement =
                     connection.prepareCall(CONTRASSEGNA_VENDUTO)) {
            statement.setString(1, sessione.getUsername());
            statement.setInt(2, codiceAnnuncio);

            if (statement.execute()) {
                try (ResultSet result = statement.getResultSet()) {
                    while (result.next()) {
                        destinatari.add(new DestinatarioNotifica(
                                result.getString("Username"),
                                result.getString("RecapitoPreferito"),
                                result.getString("Recapito")
                        ));
                    }
                }
            }
        }
        return destinatari;
    }

    // OP14: visualizzazione degli annunci pubblicati da un utente.
    public List<Annuncio> elencaPerUtente(
            SessioneUtente sessione
    ) throws SQLException {
        List<Annuncio> annunci = new ArrayList<>();

        try (Connection connection =
                     DatabaseConnection.openOperativa(sessione);
             CallableStatement statement =
                     connection.prepareCall(ELENCO_ANNUNCI_UTENTE)) {

            statement.setString(1, sessione.getUsername());

            try (ResultSet result = statement.executeQuery()) {
                while (result.next()) {
                    annunci.add(new Annuncio(
                            result.getInt("Codice"),
                            result.getString("Titolo"),
                            result.getString("DescrizioneArticolo"),
                            result.getBigDecimal("Prezzo"),
                            result.getString("Stato"),
                            sessione.getUsername(),
                            result.getInt("CodiceCategoria"),
                            result.getString("NomeCategoria")
                    ));
                }
            }
        }

        return annunci;
    }
}
