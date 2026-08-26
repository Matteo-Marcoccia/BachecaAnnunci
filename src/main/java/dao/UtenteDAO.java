package dao;

import config.DatabaseConnection;
import exception.AutenticazioneException;
import exception.RegistrazioneException;
import model.Utente;
import model.ProfiloAccesso;
import model.StatisticaUtente;
import sessione.SessioneUtente;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

public final class UtenteDAO {

    private static final String REGISTRA_UTENTE =
            "{CALL sp_RegistraUtente(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)}";

    private static final String RECUPERA_PROFILO =
            "{CALL sp_RecuperaProfiloAccesso(?)}";
    private static final String GENERA_REPORT =
            "{CALL sp_GeneraReportUtenti(?)}";

    // OP1: registrazione di un nuovo utente.
    public void registra(
            Utente utente,
            String passwordHash
    ) throws SQLException, RegistrazioneException {
        try (Connection connection = DatabaseConnection.openAccesso();
             CallableStatement statement =
                     connection.prepareCall(REGISTRA_UTENTE)) {

            statement.setString(1, utente.username());
            statement.setString(2, passwordHash);
            statement.setString(3, utente.nome());
            statement.setString(4, utente.cognome());
            statement.setString(5, utente.codiceFiscale());
            statement.setDate(6, Date.valueOf(utente.dataNascita()));
            statement.setString(7, utente.indirizzoResidenza());
            setNullableString(statement, 8, utente.indirizzoFatturazione());
            setNullableString(statement, 9, utente.email());
            setNullableString(statement, 10, utente.cellulare());
            setNullableString(statement, 11, utente.telefonoFisso());
            statement.setString(12, utente.recapitoPreferito());

            statement.executeUpdate();
        } catch (SQLIntegrityConstraintViolationException exception) {
            if (exception.getErrorCode() == 1062) {
                String message = exception.getMessage();

                if (message != null && message.contains("PRIMARY")) {
                    throw new RegistrazioneException(
                            "Username già utilizzato."
                    );
                }

                if (message != null
                        && message.contains("uq_Utente_codice_fiscale")) {
                    throw new RegistrazioneException(
                            "Codice fiscale già registrato."
                    );
                }

                throw new RegistrazioneException(
                        "Username o codice fiscale già registrato."
                );
            }

            throw exception;
        }
    }

    // OP13: recupero di PasswordHash e IsGestore per l'accesso.
    public ProfiloAccesso recuperaProfiloAccesso(
            String username
    ) throws SQLException, AutenticazioneException {
        try (Connection connection = DatabaseConnection.openAccesso();
             CallableStatement statement =
                     connection.prepareCall(RECUPERA_PROFILO)) {

            statement.setString(1, username);

            try (ResultSet result = statement.executeQuery()) {
                if (!result.next()) {
                    throw new AutenticazioneException(
                            "Username o password non validi."
                    );
                }

                return new ProfiloAccesso(
                        result.getString("Username"),
                        result.getString("PasswordHash"),
                        result.getBoolean("IsGestore")
                );
            }
        }
    }

    // OP12: recupero dei dati aggregati del report per utente.
    public List<StatisticaUtente> generaReport(
            SessioneUtente sessione
    ) throws SQLException {
        List<StatisticaUtente> statistiche = new ArrayList<>();
        try (Connection connection = DatabaseConnection.openOperativa(sessione);
             CallableStatement statement =
                     connection.prepareCall(GENERA_REPORT)) {
            statement.setString(1, sessione.getUsername());
            try (ResultSet result = statement.executeQuery()) {
                while (result.next()) {
                    statistiche.add(new StatisticaUtente(
                            result.getString("Username"),
                            result.getInt("AnnunciTotali"),
                            result.getInt("AnnunciVenduti"),
                            result.getBigDecimal("PercentualeVenduti")
                    ));
                }
            }
        }
        return statistiche;
    }

    private static void setNullableString(
            CallableStatement statement,
            int index,
            String value
    ) throws SQLException {
        if (value == null || value.isBlank()) {
            statement.setNull(index, Types.VARCHAR);
        } else {
            statement.setString(index, value.trim());
        }
    }
}
