package view;

import sessione.SessionManager;

import java.util.Scanner;

public final class MenuGestoreView {

    private final Scanner scanner;
    private final SessionManager sessionManager;
    private final AnnuncioView annuncioView;
    private final MonitoraggioView monitoraggioView;
    private final BachecaView bachecaView;
    private final ComunicazioneView comunicazioneView;
    private final CategoriaView categoriaView;
    private final ReportView reportView;

    public MenuGestoreView(
            Scanner scanner,
            AnnuncioView annuncioView,
            MonitoraggioView monitoraggioView,
            BachecaView bachecaView,
            ComunicazioneView comunicazioneView,
            CategoriaView categoriaView,
            ReportView reportView
    ) {
        this.scanner = scanner;
        this.annuncioView = annuncioView;
        this.monitoraggioView = monitoraggioView;
        this.bachecaView = bachecaView;
        this.comunicazioneView = comunicazioneView;
        this.categoriaView = categoriaView;
        this.reportView = reportView;
        this.sessionManager = SessionManager.fornisciIstanza();
    }

    public void avvia() {
        while (sessionManager.isAutenticato()) {
            mostraMenu();
            String scelta = scanner.nextLine().trim();

            switch (scelta) {
                case "1" -> annuncioView.pubblicaAnnuncio();
                case "2" -> annuncioView.gestisciPropriAnnunci();
                case "3" -> bachecaView.cercaAnnunci();
                case "4" -> comunicazioneView.visualizzaConversazioni();
                case "5" -> monitoraggioView.avvia();
                case "6" -> categoriaView.creaCategoria();
                case "7" -> reportView.mostraReport();
                case "0" -> eseguiLogout();
                default -> System.out.println("Scelta non valida.");
            }
        }
    }

    private void mostraMenu() {
        System.out.println();
        System.out.println("+----------------------------------+");
        System.out.println("|           MENU GESTORE           |");
        System.out.println("+----------------------------------+");
        System.out.println("|  1. Pubblica annuncio            |");
        System.out.println("|  2. Gestisci i miei annunci      |");
        System.out.println("|  3. Cerca annunci                |");
        System.out.println("|  4. Commenti e messaggi          |");
        System.out.println("|  5. Monitoraggio annunci         |");
        System.out.println("|  6. Crea categoria               |");
        System.out.println("|  7. Genera report                |");
        System.out.println("|  0. Logout                       |");
        System.out.println("+----------------------------------+");
        System.out.print("Scelta: ");
    }

    private void eseguiLogout() {
        sessionManager.chiudiSessione();
        System.out.println("Logout eseguito.");
    }

}
