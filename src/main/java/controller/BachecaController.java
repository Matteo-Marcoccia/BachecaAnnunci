package controller;

import dao.AnnuncioDAO;
import dao.CategoriaDAO;
import dao.CommentoPubblicoDAO;
import dao.NuovaNotaDAO;
import dao.SeguiDAO;
import model.Annuncio;
import model.Categoria;
import model.RuoloUtente;
import model.SchedaAnnuncio;
import sessione.SessionManager;
import sessione.SessioneUtente;

import java.sql.SQLException;
import java.util.List;

public final class BachecaController {

    private final AnnuncioDAO annuncioDAO;
    private final CategoriaDAO categoriaDAO;
    private final SeguiDAO seguiDAO;
    private final NuovaNotaDAO nuovaNotaDAO;
    private final CommentoPubblicoDAO commentoPubblicoDAO;
    private final SessionManager sessionManager;

    public BachecaController() {
        this.annuncioDAO = new AnnuncioDAO();
        this.categoriaDAO = new CategoriaDAO();
        this.seguiDAO = new SeguiDAO();
        this.nuovaNotaDAO = new NuovaNotaDAO();
        this.commentoPubblicoDAO = new CommentoPubblicoDAO();
        this.sessionManager = SessionManager.fornisciIstanza();
    }

    // OP4: ricerca e navigazione degli annunci.
    public List<Categoria> elencaCategorie() throws SQLException {
        return categoriaDAO.elenca(sessioneUtente());
    }

    public List<Annuncio> cercaPerCategoria(
            int codiceCategoria
    ) throws SQLException {
        return annuncioDAO.ricercaPerCategoria(
                sessioneUtente(),
                codiceCategoria
        );
    }

    public List<Annuncio> elencaAnnunciSeguiti() throws SQLException {
        return seguiDAO.elencaSeguiti(sessioneUtente());
    }

    public boolean appartieneAllaSessione(Annuncio annuncio) {
        return annuncio.autore().equals(
                sessionManager.fornisciSessioneValida().getUsername()
        );
    }

    public void segui(Annuncio annuncio) throws SQLException {
        if (appartieneAllaSessione(annuncio)) {
            throw new IllegalArgumentException(
                    "non puoi seguire un tuo annuncio."
            );
        }
        seguiDAO.segui(sessioneUtente(), annuncio.codice());
    }

    public void smettiDiSeguire(Annuncio annuncio) throws SQLException {
        seguiDAO.smettiDiSeguire(
                sessioneUtente(),
                annuncio.codice()
        );
    }

    private SessioneUtente sessioneUtente() {
        sessionManager.verificaRuolo(RuoloUtente.UTENTE);
        return sessionManager.fornisciSessioneValida();
    }

    // OP5: visualizzazione della scheda completa di dettaglio.
    public SchedaAnnuncio visualizzaDettaglio(
            int codiceAnnuncio
    ) throws SQLException {
        SessioneUtente sessione = sessioneUtente();
        return new SchedaAnnuncio(
                annuncioDAO.trovaDettaglio(sessione, codiceAnnuncio),
                nuovaNotaDAO.elencaPerAnnuncio(sessione, codiceAnnuncio),
                commentoPubblicoDAO.elencaPerAnnuncio(
                        sessione,
                        codiceAnnuncio
                )
        );
    }
}
