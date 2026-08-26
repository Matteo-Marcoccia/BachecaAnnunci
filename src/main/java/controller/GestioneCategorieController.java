package controller;

import dao.CategoriaDAO;
import model.Categoria;
import model.RuoloUtente;
import sessione.SessionManager;
import sessione.SessioneUtente;

import java.sql.SQLException;
import java.util.List;

public final class GestioneCategorieController {

    private final CategoriaDAO categoriaDAO;
    private final SessionManager sessionManager;

    public GestioneCategorieController() {
        this.categoriaDAO = new CategoriaDAO();
        this.sessionManager = SessionManager.fornisciIstanza();
    }

    public List<Categoria> elencaCategorie() throws SQLException {
        return categoriaDAO.elenca(sessioneGestore());
    }

    // OP11: creazione di una categoria riservata al Gestore.
    public void creaCategoria(
            String nome,
            Integer categoriaPadre
    ) throws SQLException {
        if (nome == null || nome.isBlank()) {
            throw new IllegalArgumentException(
                    "il nome della categoria non può essere vuoto."
            );
        }
        categoriaDAO.crea(
                sessioneGestore(),
                nome.trim(),
                categoriaPadre
        );
    }

    private SessioneUtente sessioneGestore() {
        sessionManager.verificaRuolo(RuoloUtente.GESTORE);
        return sessionManager.fornisciSessioneValida();
    }
}
