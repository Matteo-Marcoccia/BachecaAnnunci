package view;

import controller.AccessoController;
import exception.AutenticazioneException;
import exception.RegistrazioneException;
import model.Utente;
import sessione.SessionManager;
import sessione.SessioneUtente;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.Scanner;

public final class AccessoView {

    private static final DateTimeFormatter FORMATO_DATA =
            DateTimeFormatter.ofPattern("dd/MM/uuuu");

    private final Scanner scanner;
    private final AccessoController controller;
    private final SessionManager sessionManager;

    public AccessoView(
            Scanner scanner,
            AccessoController controller
    ) {
        this.scanner = scanner;
        this.controller = controller;
        this.sessionManager = SessionManager.fornisciIstanza();
    }

    public void avvia() {
        boolean termina = false;

        while (!termina && !sessionManager.isAutenticato()) {
            mostraMenu();
            String scelta = scanner.nextLine().trim();

            switch (scelta) {
                case "1" -> eseguiRegistrazione();
                case "2" -> eseguiAccesso();
                case "0" -> termina = true;
                default -> System.out.println("Scelta non valida.");
            }
        }
    }

    private void mostraMenu() {
        System.out.println();
        System.out.println("+----------------------------------+");
        System.out.println("|       BACHECA DEGLI ANNUNCI      |");
        System.out.println("+----------------------------------+");
        System.out.println("|  1. Registrazione                |");
        System.out.println("|  2. Accesso                      |");
        System.out.println("|  0. Esci                         |");
        System.out.println("+----------------------------------+");
        System.out.print("Scelta: ");
    }

    private void eseguiRegistrazione() {
        char[] password = null;
        char[] confermaPassword = null;

        try {
            System.out.println();
            System.out.println("--- REGISTRAZIONE ---");

            System.out.print("Username: ");
            String username = scanner.nextLine().trim();

            System.out.print("Password: ");
            password = scanner.nextLine().toCharArray();

            System.out.print("Conferma password: ");
            confermaPassword = scanner.nextLine().toCharArray();

            if (!Arrays.equals(password, confermaPassword)) {
                System.out.println("Le password non coincidono.");
                return;
            }

            System.out.print("Nome: ");
            String nome = scanner.nextLine().trim();

            System.out.print("Cognome: ");
            String cognome = scanner.nextLine().trim();

            System.out.print("Codice fiscale: ");
            String codiceFiscale = scanner.nextLine().trim().toUpperCase();

            System.out.print("Data di nascita (GG/MM/AAAA): ");
            LocalDate dataNascita = LocalDate.parse(
                    scanner.nextLine().trim(),
                    FORMATO_DATA
            );

            System.out.print("Indirizzo di residenza: ");
            String indirizzoResidenza = scanner.nextLine().trim();

            System.out.print("Indirizzo di fatturazione (facoltativo): ");
            String indirizzoFatturazione = scanner.nextLine().trim();

            System.out.print("Email (facoltativa): ");
            String email = scanner.nextLine().trim();

            System.out.print("Cellulare (facoltativo): ");
            String cellulare = scanner.nextLine().trim();

            System.out.print("Telefono fisso (facoltativo): ");
            String telefonoFisso = scanner.nextLine().trim();

            String recapitoPreferito = determinaRecapitoPreferito(
                    email,
                    cellulare,
                    telefonoFisso
            );

            Utente nuovoUtente = new Utente(
                    username,
                    nome,
                    cognome,
                    codiceFiscale,
                    dataNascita,
                    indirizzoResidenza,
                    indirizzoFatturazione,
                    email,
                    cellulare,
                    telefonoFisso,
                    recapitoPreferito
            );

            controller.registra(nuovoUtente, password);
            System.out.println("Registrazione completata. Ora puoi accedere.");
        } catch (DateTimeParseException exception) {
            System.out.println("Data non valida. Usa il formato GG/MM/AAAA.");
        } catch (IllegalArgumentException exception) {
            System.out.println("Dati di registrazione non validi: "
                    + exception.getMessage());
        } catch (RegistrazioneException exception) {
            System.out.println(exception.getMessage());
        } catch (IllegalStateException | SQLException exception) {
            System.err.println("Registrazione fallita: "
                    + exception.getMessage());
        } finally {
            if (password != null) {
                Arrays.fill(password, '\0');
            }
            if (confermaPassword != null) {
                Arrays.fill(confermaPassword, '\0');
            }
        }
    }

    private String determinaRecapitoPreferito(
            String email,
            String cellulare,
            String telefonoFisso
    ) {
        int recapitiPresenti = 0;
        String unicoRecapito = null;

        if (!email.isBlank()) {
            recapitiPresenti++;
            unicoRecapito = "Email";
        }
        if (!cellulare.isBlank()) {
            recapitiPresenti++;
            unicoRecapito = "Cellulare";
        }
        if (!telefonoFisso.isBlank()) {
            recapitiPresenti++;
            unicoRecapito = "TelefonoFisso";
        }

        if (recapitiPresenti == 0) {
            throw new IllegalArgumentException(
                    "deve essere indicato almeno un recapito."
            );
        }

        if (recapitiPresenti == 1) {
            System.out.println("Recapito preferito impostato automaticamente: "
                    + unicoRecapito);
            return unicoRecapito;
        }

        while (true) {
            System.out.println("Scegli il recapito preferito:");
            if (!email.isBlank()) {
                System.out.println("1. Email");
            }
            if (!cellulare.isBlank()) {
                System.out.println("2. Cellulare");
            }
            if (!telefonoFisso.isBlank()) {
                System.out.println("3. Telefono fisso");
            }
            System.out.print("Scelta: ");

            String scelta = scanner.nextLine().trim();

            if (scelta.equals("1") && !email.isBlank()) {
                return "Email";
            }
            if (scelta.equals("2") && !cellulare.isBlank()) {
                return "Cellulare";
            }
            if (scelta.equals("3") && !telefonoFisso.isBlank()) {
                return "TelefonoFisso";
            }

            System.out.println("Scelta non valida.");
        }
    }

    private void eseguiAccesso() {
        System.out.print("Username: ");
        String username = scanner.nextLine().trim();

        System.out.print("Password: ");
        char[] password = scanner.nextLine().toCharArray();

        try {
            SessioneUtente sessione =
                    controller.effettuaAccesso(username, password);

            System.out.println("Accesso riuscito.");
            System.out.println("Username: " + sessione.getUsername());
            System.out.println("Ruolo verificato: " + sessione.getRuolo());
        } catch (AutenticazioneException exception) {
            System.out.println(exception.getMessage());
        } catch (IllegalStateException | SQLException exception) {
            System.err.println("Errore durante l'accesso: "
                    + exception.getMessage());
        } finally {
            Arrays.fill(password, '\0');
        }
    }
}
