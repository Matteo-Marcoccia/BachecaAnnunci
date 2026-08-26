package config;

import sessione.SessioneUtente;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;

public final class DatabaseConnection {

    private static final String DEFAULT_URL =
            "jdbc:mysql://localhost:3306/BachecaAnnunci"
                    + "?useSSL=false"
                    + "&allowPublicKeyRetrieval=true"
                    + "&allowMultiQueries=false"
                    + "&serverTimezone=Europe/Rome";

    private DatabaseConnection() {
    }

    public static Connection openAccesso() throws SQLException {
        return open(
                "DB_ACCESS_USER",
                "DB_ACCESS_PASSWORD"
        );
    }

    public static Connection openOperativa(
            SessioneUtente sessione
    ) throws SQLException {
        Connection connection = sessione.isGestore()
                ? open("DB_MANAGER_USER", "DB_MANAGER_PASSWORD")
                : open("DB_USER_USER", "DB_USER_PASSWORD");

        try {
            verificaProfilo(connection, sessione.getUsername());
            return connection;
        } catch (SQLException exception) {
            connection.close();
            throw exception;
        }
    }

    private static Connection open(
            String usernameVariable,
            String passwordVariable
    ) throws SQLException {
        String url = System.getenv().getOrDefault("DB_URL", DEFAULT_URL);
        String username = requireEnvironmentVariable(usernameVariable);
        String password = requireEnvironmentVariable(passwordVariable);

        return DriverManager.getConnection(url, username, password);
    }

    private static void verificaProfilo(
            Connection connection,
            String username
    ) throws SQLException {
        try (CallableStatement statement = connection.prepareCall(
                "{CALL sp_VerificaProfiloSessione(?)}"
        )) {
            statement.setString(1, username);

            try (ResultSet result = statement.executeQuery()) {
                if (!result.next()) {
                    throw new SQLException(
                            "Verifica del profilo non completata."
                    );
                }
            }
        }
    }

    private static String requireEnvironmentVariable(String name) {
        String value = System.getenv(name);

        if (value == null || value.isBlank()) {
            throw new IllegalStateException(
                    "Variabile d'ambiente mancante: " + name
            );
        }

        return value;
    }
}
