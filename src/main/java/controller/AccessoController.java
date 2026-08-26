package controller;

import config.DatabaseConnection;
import exception.AutenticazioneException;
import exception.RegistrazioneException;
import model.Utente;
import service.AutenticazioneService;
import service.RegistrazioneService;
import sessione.SessionManager;
import sessione.SessioneUtente;

import java.sql.Connection;
import java.sql.SQLException;

public final class AccessoController {

    private final AutenticazioneService autenticazioneService;
    private final RegistrazioneService registrazioneService;
    private final SessionManager sessionManager;

    public AccessoController() {
        this.autenticazioneService = new AutenticazioneService();
        this.registrazioneService = new RegistrazioneService();
        this.sessionManager = SessionManager.fornisciIstanza();
    }

    // OP1: registrazione di un nuovo utente.
    public void registra(
            Utente utente,
            char[] password
    ) throws SQLException, RegistrazioneException {
        registrazioneService.registra(utente, password);
    }

    // OP13: autenticazione e creazione della sessione.
    public SessioneUtente effettuaAccesso(
            String username,
            char[] password
    ) throws SQLException, AutenticazioneException {
        SessioneUtente sessione =
                autenticazioneService.autentica(username, password);

        try (Connection ignored =
                     DatabaseConnection.openOperativa(sessione)) {
            sessionManager.apriSessione(sessione);
            return sessione;
        }
    }
}
