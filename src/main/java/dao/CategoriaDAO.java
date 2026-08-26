package dao;

import config.DatabaseConnection;
import model.Categoria;
import sessione.SessioneUtente;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public final class CategoriaDAO {

    private static final String ELENCO_CATEGORIE =
            "{CALL sp_ElencoCategorie()}";
    private static final String CREA_CATEGORIA =
            "{CALL sp_CreaCategoria(?, ?, ?)}";

    // Procedura di supporto a OP2, OP4 e OP11.
    public List<Categoria> elenca(SessioneUtente sessione)
            throws SQLException {
        List<Categoria> categorie = new ArrayList<>();

        try (Connection connection =
                     DatabaseConnection.openOperativa(sessione);
             CallableStatement statement =
                     connection.prepareCall(ELENCO_CATEGORIE);
             ResultSet result = statement.executeQuery()) {

            while (result.next()) {
                int codicePadre = result.getInt("CategoriaPadre");
                Integer categoriaPadre = result.wasNull()
                        ? null
                        : codicePadre;

                categorie.add(new Categoria(
                        result.getInt("CodiceCategoria"),
                        result.getString("Nome"),
                        categoriaPadre,
                        result.getString("GestoreCreatore")
                ));
            }
        }

        return categorie;
    }

    // OP4: lettura della categoria e delle relative sottocategorie.
    // OP11: creazione e collocazione di una nuova categoria.
    public void crea(
            SessioneUtente sessione,
            String nome,
            Integer categoriaPadre
    ) throws SQLException {
        try (Connection connection = DatabaseConnection.openOperativa(sessione);
             CallableStatement statement =
                     connection.prepareCall(CREA_CATEGORIA)) {
            statement.setString(1, nome);
            if (categoriaPadre == null) {
                statement.setNull(2, java.sql.Types.INTEGER);
            } else {
                statement.setInt(2, categoriaPadre);
            }
            statement.setString(3, sessione.getUsername());

            statement.executeUpdate();
        }
    }
}
