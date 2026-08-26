package app;

import controller.AccessoController;
import controller.BachecaController;
import controller.ComunicazioneController;
import controller.GestioneAnnunciController;
import controller.GestioneCategorieController;
import controller.MonitoraggioController;
import controller.ReportController;
import sessione.SessionManager;
import sessione.SessioneUtente;
import view.AccessoView;
import view.AnnuncioView;
import view.BachecaView;
import view.ComunicazioneView;
import view.CategoriaView;
import view.MenuGestoreView;
import view.MenuUtenteView;
import view.MonitoraggioView;
import view.ReportView;

import java.util.Scanner;

public class Main {

    public static void main(String[] args) {
        try (Scanner scanner = new Scanner(System.in)) {
            AccessoController accessoController =
                    new AccessoController();
            AccessoView accessoView =
                    new AccessoView(scanner, accessoController);
            GestioneAnnunciController gestioneAnnunciController =
                    new GestioneAnnunciController();
            BachecaController bachecaController = new BachecaController();
            ComunicazioneController comunicazioneController =
                    new ComunicazioneController();
            ComunicazioneView comunicazioneView =
                    new ComunicazioneView(scanner, comunicazioneController);
            BachecaView bachecaView =
                    new BachecaView(
                            scanner,
                            bachecaController,
                            comunicazioneView
                    );
            AnnuncioView annuncioView =
                    new AnnuncioView(
                            scanner,
                            gestioneAnnunciController,
                            bachecaView
                    );
            MonitoraggioController monitoraggioController =
                    new MonitoraggioController();
            MonitoraggioView monitoraggioView =
                    new MonitoraggioView(
                            scanner,
                            monitoraggioController,
                            bachecaView
                    );
            GestioneCategorieController gestioneCategorieController =
                    new GestioneCategorieController();
            CategoriaView categoriaView = new CategoriaView(
                    scanner,
                    gestioneCategorieController
            );
            ReportController reportController = new ReportController();
            ReportView reportView = new ReportView(reportController);
            MenuUtenteView menuUtenteView =
                    new MenuUtenteView(
                            scanner,
                            annuncioView,
                            monitoraggioView,
                            bachecaView,
                            comunicazioneView
                    );
            MenuGestoreView menuGestoreView =
                    new MenuGestoreView(
                            scanner,
                            annuncioView,
                            monitoraggioView,
                            bachecaView,
                            comunicazioneView,
                            categoriaView,
                            reportView
                    );
            SessionManager sessionManager =
                    SessionManager.fornisciIstanza();

            boolean applicazioneAttiva = true;

            while (applicazioneAttiva) {
                accessoView.avvia();

                if (!sessionManager.isAutenticato()) {
                    applicazioneAttiva = false;
                    continue;
                }

                SessioneUtente sessione =
                        sessionManager.fornisciSessioneValida();

                if (sessione.isGestore()) {
                    menuGestoreView.avvia();
                } else {
                    menuUtenteView.avvia();
                }
            }

            System.out.println("Applicazione terminata.");
        }
    }
}
