package service;

import dao.UtenteDAO;
import exception.RegistrazioneException;
import model.Utente;
import security.PasswordHasher;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.regex.Pattern;

public final class RegistrazioneService {

    private static final Pattern CODICE_FISCALE =
            Pattern.compile("^[A-Z0-9]{16}$");
    private static final Pattern EMAIL =
            Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");
    private static final Pattern TELEFONO =
            Pattern.compile("^\\+?[0-9]{7,15}$");

    private final UtenteDAO utenteDAO;

    public RegistrazioneService() {
        this.utenteDAO = new UtenteDAO();
    }

    public void registra(
            Utente utente,
            char[] password
    ) throws SQLException, RegistrazioneException {
        valida(utente);
        validaPassword(password);
        String passwordHash = PasswordHasher.hash(password);
        utenteDAO.registra(utente, passwordHash);
    }

    private static void validaPassword(char[] password) {
        if (password == null || password.length == 0) {
            throw new IllegalArgumentException(
                    "la password deve contenere almeno un carattere."
            );
        }
    }

    private static void valida(Utente utente) {
        if (utente.username().isBlank()
                || utente.nome().isBlank()
                || utente.cognome().isBlank()
                || utente.indirizzoResidenza().isBlank()) {
            throw new IllegalArgumentException(
                    "i campi obbligatori non possono essere vuoti."
            );
        }

        if (!CODICE_FISCALE.matcher(utente.codiceFiscale()).matches()) {
            throw new IllegalArgumentException(
                    "il codice fiscale deve contenere 16 caratteri alfanumerici."
            );
        }

        if (utente.dataNascita().isAfter(LocalDate.now())) {
            throw new IllegalArgumentException(
                    "la data di nascita non può essere futura."
            );
        }

        if (!utente.email().isBlank()
                && !EMAIL.matcher(utente.email()).matches()) {
            throw new IllegalArgumentException(
                    "il formato dell'email non è valido."
            );
        }

        validaTelefono(utente.cellulare(), "cellulare");
        validaTelefono(utente.telefonoFisso(), "telefono fisso");
    }

    private static void validaTelefono(String numero, String nomeCampo) {
        if (!numero.isBlank() && !TELEFONO.matcher(numero).matches()) {
            throw new IllegalArgumentException(
                    "il " + nomeCampo
                            + " deve contenere da 7 a 15 cifre,"
                            + " con un eventuale + iniziale."
            );
        }
    }
}
