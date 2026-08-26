package sessione;

import model.RuoloUtente;

public final class SessionManager {

    private static final SessionManager INSTANCE = new SessionManager();

    private SessioneUtente sessioneAttiva;

    private SessionManager() {
    }

    public static SessionManager fornisciIstanza() {
        return INSTANCE;
    }

    public synchronized void apriSessione(SessioneUtente sessione) {
        if (sessioneAttiva != null) {
            throw new IllegalStateException("Esiste gia una sessione attiva.");
        }

        sessioneAttiva = sessione;
    }

    public synchronized SessioneUtente fornisciSessioneValida() {
        if (sessioneAttiva == null) {
            throw new IllegalStateException("Nessuna sessione attiva.");
        }

        return sessioneAttiva;
    }

    public synchronized void verificaRuolo(RuoloUtente ruoloRichiesto) {
        SessioneUtente sessione = fornisciSessioneValida();

        if (ruoloRichiesto == RuoloUtente.GESTORE
                && sessione.getRuolo() != RuoloUtente.GESTORE) {
            throw new IllegalStateException(
                    "Sessione non autorizzata per questa operazione."
            );
        }
    }

    public synchronized boolean isAutenticato() {
        return sessioneAttiva != null;
    }

    public synchronized void chiudiSessione() {
        sessioneAttiva = null;
    }
}
