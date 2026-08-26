package controller;

import dao.UtenteDAO;
import model.RuoloUtente;
import model.StatisticaUtente;
import sessione.SessionManager;

import java.sql.SQLException;
import java.util.List;

public final class ReportController {

    private final UtenteDAO utenteDAO;
    private final SessionManager sessionManager;

    public ReportController() {
        this.utenteDAO = new UtenteDAO();
        this.sessionManager = SessionManager.fornisciIstanza();
    }

    public List<StatisticaUtente> generaReport() throws SQLException {
        sessionManager.verificaRuolo(RuoloUtente.GESTORE);
        return utenteDAO.generaReport(
                sessionManager.fornisciSessioneValida()
        );
    }
}
