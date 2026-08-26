package dao;

import config.DatabaseConnection;
import model.CommentoPubblico;
import model.DestinatarioNotifica;
import sessione.SessioneUtente;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public final class CommentoPubblicoDAO {

    private static final String ELENCO_COMMENTI =
            "{CALL sp_ElencoCommentiAnnuncio(?)}";
    private static final String INSERISCI_COMMENTO =
            "{CALL sp_InserisciCommento(?, ?, ?)}";

    // OP5: lettura dei commenti della scheda di dettaglio.
    public List<CommentoPubblico> elencaPerAnnuncio(
            SessioneUtente sessione,
            int codiceAnnuncio
    ) throws SQLException {
        List<CommentoPubblico> commenti = new ArrayList<>();
        try (Connection connection = DatabaseConnection.openOperativa(sessione);
             CallableStatement statement =
                     connection.prepareCall(ELENCO_COMMENTI)) {
            statement.setInt(1, codiceAnnuncio);
            try (ResultSet result = statement.executeQuery()) {
                while (result.next()) {
                    commenti.add(new CommentoPubblico(
                            result.getInt("CodiceCommento"),
                            result.getString("Testo"),
                            result.getTimestamp("DataOra").toLocalDateTime(),
                            codiceAnnuncio,
                            result.getString("Autore")
                    ));
                }
            }
        }
        return commenti;
    }

    // OP6: inserimento di un commento e recupero dei destinatari.
    public List<DestinatarioNotifica> inserisci(
            SessioneUtente sessione,
            int codiceAnnuncio,
            String testo
    ) throws SQLException {
        List<DestinatarioNotifica> destinatari = new ArrayList<>();
        try (Connection connection = DatabaseConnection.openOperativa(sessione);
             CallableStatement statement =
                     connection.prepareCall(INSERISCI_COMMENTO)) {
            statement.setString(1, testo);
            statement.setInt(2, codiceAnnuncio);
            statement.setString(3, sessione.getUsername());

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
}
