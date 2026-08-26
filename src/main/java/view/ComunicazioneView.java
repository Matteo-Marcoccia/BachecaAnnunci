package view;

import controller.ComunicazioneController;
import model.Annuncio;
import model.ConversazionePrivata;
import model.MessaggioPrivato;

import java.sql.SQLException;
import java.util.Scanner;
import java.util.List;

public final class ComunicazioneView {

    private final Scanner scanner;
    private final ComunicazioneController controller;

    public ComunicazioneView(
            Scanner scanner,
            ComunicazioneController controller
    ) {
        this.scanner = scanner;
        this.controller = controller;
    }

    // OP6: inserimento di un commento pubblico.
    public void inserisciCommento(Annuncio annuncio) {
        try {
            System.out.print("Testo del commento: ");
            String testo = scanner.nextLine();
            int destinatari = controller.inserisciCommento(
                    annuncio.codice(),
                    testo
            );
            System.out.println("Commento pubblicato correttamente.");
            System.out.println("Utenti notificati: " + destinatari);
            attendiInvioSeNecessario(destinatari);
        } catch (IllegalArgumentException exception) {
            System.out.println("Dati non validi: " + exception.getMessage());
        } catch (SQLException | IllegalStateException exception) {
            System.err.println("Inserimento del commento fallito: "
                    + exception.getMessage());
        }
    }

    private void attendiInvioSeNecessario(int destinatari) {
        if (destinatari > 0) {
            System.out.print("Premi Invio per continuare...");
            scanner.nextLine();
        }
    }

    // OP7: invio di un messaggio dalla scheda annuncio.
    public void inviaMessaggio(Annuncio annuncio) {
        try {
            System.out.print("Testo del messaggio: ");
            controller.inviaMessaggio(
                    annuncio.codice(),
                    annuncio.autore(),
                    scanner.nextLine()
            );
            System.out.println("Messaggio inviato correttamente.");
            attendiInvioSeNecessario(1);
        } catch (IllegalArgumentException exception) {
            System.out.println("Dati non validi: " + exception.getMessage());
        } catch (SQLException | IllegalStateException exception) {
            System.err.println("Invio del messaggio fallito: "
                    + exception.getMessage());
        }
    }

    // OP15: elenco e visualizzazione delle conversazioni.
    public void visualizzaConversazioni() {
        try {
            List<ConversazionePrivata> conversazioni =
                    controller.elencaConversazioni();
            if (conversazioni.isEmpty()) {
                System.out.println("Non sono presenti conversazioni.");
                return;
            }

            System.out.println();
            System.out.println("--- CONVERSAZIONI PRIVATE ---");
            for (int index = 0; index < conversazioni.size(); index++) {
                ConversazionePrivata conversazione = conversazioni.get(index);
                System.out.printf("%d. %s | con %s%n",
                        index + 1,
                        conversazione.titoloAnnuncio(),
                        conversazione.altroUtente());
            }
            System.out.println("0. Torna al menu");
            System.out.print("Conversazione: ");
            int scelta = Integer.parseInt(scanner.nextLine().trim());
            if (scelta == 0) {
                return;
            }
            if (scelta < 1 || scelta > conversazioni.size()) {
                System.out.println("Conversazione non valida.");
                return;
            }

            ConversazionePrivata conversazione =
                    conversazioni.get(scelta - 1);
            stampaConversazione(conversazione);
        } catch (NumberFormatException exception) {
            System.out.println("Scelta non valida.");
        } catch (SQLException | IllegalStateException exception) {
            System.err.println("Caricamento delle conversazioni fallito: "
                    + exception.getMessage());
        }
    }

    private void stampaConversazione(
            ConversazionePrivata conversazione
    ) throws SQLException {
        List<MessaggioPrivato> messaggi =
                controller.visualizzaConversazione(conversazione);

        System.out.println();
        System.out.println("--- " + conversazione.titoloAnnuncio()
                + " | " + conversazione.altroUtente() + " ---");
        for (MessaggioPrivato messaggio : messaggi) {
            System.out.println("[" + messaggio.dataOra() + "] "
                    + messaggio.mittente() + ": " + messaggio.testo());
        }

        System.out.println("1. Rispondi");
        System.out.println("0. Torna al menu");
        System.out.print("Operazione: ");
        if (scanner.nextLine().trim().equals("1")) {
            System.out.print("Testo del messaggio: ");
            controller.inviaMessaggio(
                    conversazione.codiceAnnuncio(),
                    conversazione.altroUtente(),
                    scanner.nextLine()
            );
            System.out.println("Messaggio inviato correttamente.");
            attendiInvioSeNecessario(1);
        }
    }
}
