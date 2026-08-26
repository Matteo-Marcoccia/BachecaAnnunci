package dao;

import config.DatabaseConnection;
import model.NuovaNota;
import model.DestinatarioNotifica;
import sessione.SessioneUtente;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public final class NuovaNotaDAO {

    private static final String PUBBLICA_NOTA =
            "{CALL sp_PubblicaNota(?, ?, ?)}";
    private static final String ELENCO_NOTE =
            "{CALL sp_ElencoNoteAnnuncio(?)}";

    // OP3: pubblicazione di una nota integrativa e recupero dei follower.
    public List<DestinatarioNotifica> pubblica(
            SessioneUtente sessione,
            int codiceAnnuncio,
            String testo
    ) throws SQLException {
        try (Connection connection =
                     DatabaseConnection.openOperativa(sessione);
             CallableStatement statement =
                     connection.prepareCall(PUBBLICA_NOTA)) {

            statement.setString(1, sessione.getUsername());
            statement.setInt(2, codiceAnnuncio);
            statement.setString(3, testo);

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

    // OP5: lettura delle note della scheda di dettaglio.
    public List<NuovaNota> elencaPerAnnuncio(
            SessioneUtente sessione,
            int codiceAnnuncio
    ) throws SQLException {
        List<NuovaNota> note = new ArrayList<>();
        try (Connection connection = DatabaseConnection.openOperativa(sessione);
             CallableStatement statement = connection.prepareCall(ELENCO_NOTE)) {
            statement.setInt(1, codiceAnnuncio);
            try (ResultSet result = statement.executeQuery()) {
                while (result.next()) {
                    note.add(new NuovaNota(
                            result.getInt("CodiceNota"),
                            result.getString("TestoNota"),
                            result.getTimestamp("DataOra").toLocalDateTime(),
                            codiceAnnuncio
                    ));
                }
            }
        }
        return note;
    }
}
