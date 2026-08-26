package controller;

import dao.CommentoPubblicoDAO;
import dao.MessaggioPrivatoDAO;
import model.ConversazionePrivata;
import model.DestinatarioNotifica;
import model.RuoloUtente;
import model.MessaggioPrivato;
import service.NotificaService;
import sessione.SessionManager;
import sessione.SessioneUtente;

import java.sql.SQLException;
import java.util.List;

public final class ComunicazioneController {

    private final CommentoPubblicoDAO commentoPubblicoDAO;
    private final NotificaService notificaService;
    private final SessionManager sessionManager;
    private final MessaggioPrivatoDAO messaggioPrivatoDAO;

    public ComunicazioneController() {
        this.commentoPubblicoDAO = new CommentoPubblicoDAO();
        this.notificaService = new NotificaService();
        this.sessionManager = SessionManager.fornisciIstanza();
        this.messaggioPrivatoDAO = new MessaggioPrivatoDAO();
    }

    // OP6: inserimento di un commento pubblico.
    public int inserisciCommento(
            int codiceAnnuncio,
            String testo
    ) throws SQLException {
        if (testo == null || testo.isBlank()) {
            throw new IllegalArgumentException(
                    "il commento non può essere vuoto."
            );
        }

        SessioneUtente sessione = sessioneUtente();
        List<DestinatarioNotifica> destinatari =
                commentoPubblicoDAO.inserisci(
                        sessione,
                        codiceAnnuncio,
                        testo.trim()
                );

        notificaService.inviaSimulate(
                destinatari,
                "È stato inserito un nuovo commento sull'annuncio."
        );
        return destinatari.size();
    }

    private SessioneUtente sessioneUtente() {
        sessionManager.verificaRuolo(RuoloUtente.UTENTE);
        return sessionManager.fornisciSessioneValida();
    }

    // OP7: invio di un messaggio privato.
    public void inviaMessaggio(
            int codiceAnnuncio,
            String destinatario,
            String testo
    ) throws SQLException {
        if (testo == null || testo.isBlank()) {
            throw new IllegalArgumentException(
                    "il messaggio non può essere vuoto."
            );
        }
        DestinatarioNotifica destinatarioNotifica = messaggioPrivatoDAO.invia(
                sessioneUtente(),
                codiceAnnuncio,
                destinatario,
                testo.trim()
        );
        notificaService.inviaSimulate(
                List.of(destinatarioNotifica),
                "Hai ricevuto un nuovo messaggio privato relativo a un annuncio."
        );
    }

    // OP15: visualizzazione di una conversazione.
    public List<ConversazionePrivata> elencaConversazioni()
            throws SQLException {
        return messaggioPrivatoDAO.elencaConversazioni(sessioneUtente());
    }

    public List<MessaggioPrivato> visualizzaConversazione(
            ConversazionePrivata conversazione
    ) throws SQLException {
        return messaggioPrivatoDAO.visualizzaConversazione(
                sessioneUtente(),
                conversazione
        );
    }
}
