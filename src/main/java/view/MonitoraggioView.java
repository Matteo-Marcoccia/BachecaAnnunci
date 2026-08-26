package view;

import controller.MonitoraggioController;
import model.Annuncio;

import java.sql.SQLException;
import java.util.List;
import java.util.Scanner;

public final class MonitoraggioView {

    private final Scanner scanner;
    private final MonitoraggioController controller;
    private final BachecaView bachecaView;

    public MonitoraggioView(
            Scanner scanner,
            MonitoraggioController controller,
            BachecaView bachecaView
    ) {
        this.scanner = scanner;
        this.controller = controller;
        this.bachecaView = bachecaView;
    }

    // OP8: iscrizione e disiscrizione dal monitoraggio.
    public void avvia() {
        try {
            List<Annuncio> annunci = controller.elencaSeguiti();
            Annuncio scelto = scegliAnnuncio(annunci);
            if (scelto == null) {
                return;
            }

            System.out.println("1. Visualizza dettaglio");
            System.out.println("2. Smetti di seguire");
            System.out.println("0. Torna al menu");
            System.out.print("Operazione: ");
            switch (scanner.nextLine().trim()) {
                case "1" -> bachecaView.mostraDettaglio(scelto, true);
                case "2" -> {
                    controller.smettiDiSeguire(scelto.codice());
                    System.out.println(
                            "Non segui più l'annuncio selezionato."
                    );
                }
                case "0" -> { }
                default -> System.out.println("Operazione non valida.");
            }
        } catch (SQLException | IllegalStateException exception) {
            System.err.println("Operazione fallita: " + exception.getMessage());
        }
    }

    private Annuncio scegliAnnuncio(List<Annuncio> annunci) {
        if (annunci.isEmpty()) {
            System.out.println("Non stai seguendo alcun annuncio.");
            return null;
        }

        for (int index = 0; index < annunci.size(); index++) {
            Annuncio annuncio = annunci.get(index);
            System.out.printf(
                    "%d. %s | %s | %s euro | autore: %s%n",
                    index + 1,
                    annuncio.titolo(),
                    annuncio.nomeCategoria(),
                    annuncio.prezzo(),
                    annuncio.autore()
            );
        }
        System.out.println("0. Annulla");
        System.out.print("Annuncio: ");

        try {
            int scelta = Integer.parseInt(scanner.nextLine().trim());
            if (scelta == 0) {
                return null;
            }
            if (scelta < 1 || scelta > annunci.size()) {
                System.out.println("Annuncio non valido.");
                return null;
            }
            return annunci.get(scelta - 1);
        } catch (NumberFormatException exception) {
            System.out.println("Scelta non valida.");
            return null;
        }
    }

    // Qui mostreremo l'esito delle notifiche simulate.
}
