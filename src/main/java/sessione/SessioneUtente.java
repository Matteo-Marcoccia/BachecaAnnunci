package sessione;

import model.RuoloUtente;

public final class SessioneUtente {

    private final String username;
    private final RuoloUtente ruolo;

    public SessioneUtente(String username, RuoloUtente ruolo) {
        this.username = username;
        this.ruolo = ruolo;
    }

    public String getUsername() {
        return username;
    }

    public RuoloUtente getRuolo() {
        return ruolo;
    }

    public boolean isGestore() {
        return ruolo == RuoloUtente.GESTORE;
    }
}
