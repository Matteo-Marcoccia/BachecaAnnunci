package service;

import dao.UtenteDAO;
import exception.AutenticazioneException;
import model.ProfiloAccesso;
import model.RuoloUtente;
import security.PasswordHasher;
import sessione.SessioneUtente;

import java.sql.SQLException;

public final class AutenticazioneService {

    private final UtenteDAO utenteDAO;

    public AutenticazioneService() {
        this.utenteDAO = new UtenteDAO();
    }

    public SessioneUtente autentica(
            String username,
            char[] password
    ) throws SQLException, AutenticazioneException {
        ProfiloAccesso profilo =
                utenteDAO.recuperaProfiloAccesso(username);

        if (!PasswordHasher.verify(password, profilo.passwordHash())) {
            throw new AutenticazioneException(
                    "Username o password non validi."
            );
        }

        return new SessioneUtente(
                profilo.username(),
                profilo.gestore()
                        ? RuoloUtente.GESTORE
                        : RuoloUtente.UTENTE
        );
    }
}
