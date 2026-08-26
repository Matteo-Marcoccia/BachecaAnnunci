package dao;

import config.DatabaseConnection;
import model.Annuncio;
import sessione.SessioneUtente;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public final class SeguiDAO {

    private static final String ELENCO_SEGUITI =
            "{CALL sp_ElencoAnnunciSeguiti(?)}";
    private static final String SEGUI =
            "{CALL sp_SeguiAnnuncio(?, ?)}";
    private static final String SMETTI_DI_SEGUIRE =
            "{CALL sp_SmettiDiSeguireAnnuncio(?, ?)}";

    // Qui centralizzeremo l'accesso alla tabella Segui.
    // OP3, OP9 e OP10: recupero dei destinatari delle notifiche.
    // OP8: iscrizione e disiscrizione dal monitoraggio di un annuncio.
    public List<Annuncio> elencaSeguiti(
            SessioneUtente sessione
    ) throws SQLException {
        return elenca(sessione);
    }

    public void segui(
            SessioneUtente sessione,
            int codiceAnnuncio
    ) throws SQLException {
        eseguiOperazione(sessione, codiceAnnuncio, SEGUI);
    }

    public void smettiDiSeguire(
            SessioneUtente sessione,
            int codiceAnnuncio
    ) throws SQLException {
        eseguiOperazione(sessione, codiceAnnuncio, SMETTI_DI_SEGUIRE);
    }

    private List<Annuncio> elenca(SessioneUtente sessione) throws SQLException {
        List<Annuncio> annunci = new ArrayList<>();
        try (Connection connection = DatabaseConnection.openOperativa(sessione);
             CallableStatement statement = connection.prepareCall(ELENCO_SEGUITI)) {
            statement.setString(1, sessione.getUsername());
            try (ResultSet result = statement.executeQuery()) {
                while (result.next()) {
                    annunci.add(new Annuncio(
                            result.getInt("Codice"),
                            result.getString("Titolo"),
                            result.getString("DescrizioneArticolo"),
                            result.getBigDecimal("Prezzo"),
                            result.getString("Stato"),
                            result.getString("Autore"),
                            result.getInt("CodiceCategoria"),
                            result.getString("NomeCategoria")
                    ));
                }
            }
        }
        return annunci;
    }

    private void eseguiOperazione(
            SessioneUtente sessione,
            int codiceAnnuncio,
            String procedura
    ) throws SQLException {
        try (Connection connection = DatabaseConnection.openOperativa(sessione);
             CallableStatement statement = connection.prepareCall(procedura)) {
            statement.setString(1, sessione.getUsername());
            statement.setInt(2, codiceAnnuncio);
            statement.executeUpdate();
        }
    }
}
