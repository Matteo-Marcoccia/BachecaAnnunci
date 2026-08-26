package dao;

import config.DatabaseConnection;
import model.ConversazionePrivata;
import model.DestinatarioNotifica;
import model.MessaggioPrivato;
import sessione.SessioneUtente;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public final class MessaggioPrivatoDAO {

    private static final String INVIA_MESSAGGIO =
            "{CALL sp_InviaMessaggioPrivato(?, ?, ?, ?)}";
    private static final String ELENCO_CONVERSAZIONI =
            "{CALL sp_ElencoConversazioniUtente(?)}";
    private static final String VISUALIZZA_CONVERSAZIONE =
            "{CALL sp_VisualizzaConversazione(?, ?, ?)}";

    // OP7: invio di un messaggio privato.
    public DestinatarioNotifica invia(
            SessioneUtente sessione,
            int codiceAnnuncio,
            String destinatario,
            String testo
    ) throws SQLException {
        try (Connection connection = DatabaseConnection.openOperativa(sessione);
             CallableStatement statement = connection.prepareCall(INVIA_MESSAGGIO)) {
            statement.setString(1, testo);
            statement.setInt(2, codiceAnnuncio);
            statement.setString(3, sessione.getUsername());
            statement.setString(4, destinatario);
            try (ResultSet result = statement.executeQuery()) {
                if (!result.next()) {
                    throw new SQLException(
                            "Recapito del destinatario non restituito."
                    );
                }
                return new DestinatarioNotifica(
                        result.getString("Username"),
                        result.getString("RecapitoPreferito"),
                        result.getString("Recapito")
                );
            }
        }
    }

    // OP15: visualizzazione di una conversazione relativa a un annuncio.
    public List<ConversazionePrivata> elencaConversazioni(
            SessioneUtente sessione
    ) throws SQLException {
        List<ConversazionePrivata> conversazioni = new ArrayList<>();
        try (Connection connection = DatabaseConnection.openOperativa(sessione);
             CallableStatement statement =
                     connection.prepareCall(ELENCO_CONVERSAZIONI)) {
            statement.setString(1, sessione.getUsername());
            try (ResultSet result = statement.executeQuery()) {
                while (result.next()) {
                    conversazioni.add(new ConversazionePrivata(
                            result.getInt("CodiceAnnuncio"),
                            result.getString("TitoloAnnuncio"),
                            result.getString("AltroUtente")
                    ));
                }
            }
        }
        return conversazioni;
    }

    public List<MessaggioPrivato> visualizzaConversazione(
            SessioneUtente sessione,
            ConversazionePrivata conversazione
    ) throws SQLException {
        List<MessaggioPrivato> messaggi = new ArrayList<>();
        try (Connection connection = DatabaseConnection.openOperativa(sessione);
             CallableStatement statement =
                     connection.prepareCall(VISUALIZZA_CONVERSAZIONE)) {
            statement.setString(1, sessione.getUsername());
            statement.setString(2, conversazione.altroUtente());
            statement.setInt(3, conversazione.codiceAnnuncio());
            try (ResultSet result = statement.executeQuery()) {
                while (result.next()) {
                    messaggi.add(new MessaggioPrivato(
                            result.getInt("CodiceMessaggio"),
                            result.getString("Testo"),
                            result.getTimestamp("DataOra").toLocalDateTime(),
                            conversazione.codiceAnnuncio(),
                            result.getString("Mittente"),
                            result.getString("Destinatario")
                    ));
                }
            }
        }
        return messaggi;
    }
}
