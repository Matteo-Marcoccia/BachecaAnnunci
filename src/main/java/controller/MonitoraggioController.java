package controller;

import dao.SeguiDAO;
import model.Annuncio;
import model.RuoloUtente;
import sessione.SessionManager;
import sessione.SessioneUtente;

import java.sql.SQLException;
import java.util.List;

public final class MonitoraggioController {

    private final SeguiDAO seguiDAO;
    private final SessionManager sessionManager;

    public MonitoraggioController() {
        this.seguiDAO = new SeguiDAO();
        this.sessionManager = SessionManager.fornisciIstanza();
    }

    // OP8: seguire o smettere di seguire un annuncio.
    public List<Annuncio> elencaSeguiti() throws SQLException {
        return seguiDAO.elencaSeguiti(sessioneUtente());
    }

    public void smettiDiSeguire(int codiceAnnuncio) throws SQLException {
        seguiDAO.smettiDiSeguire(sessioneUtente(), codiceAnnuncio);
    }

    private SessioneUtente sessioneUtente() {
        sessionManager.verificaRuolo(RuoloUtente.UTENTE);
        return sessionManager.fornisciSessioneValida();
    }

    // Qui coordineremo le notifiche prodotte da OP3, OP9 e OP10.
}
