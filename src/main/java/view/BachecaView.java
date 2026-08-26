package view;

import controller.BachecaController;
import model.Annuncio;
import model.Categoria;
import model.CommentoPubblico;
import model.DettaglioAnnuncio;
import model.NuovaNota;
import model.SchedaAnnuncio;

import java.sql.SQLException;
import java.util.HashSet;
import java.util.List;
import java.util.Scanner;
import java.util.Set;

public final class BachecaView {

    private final Scanner scanner;
    private final BachecaController controller;
    private final ComunicazioneView comunicazioneView;

    public BachecaView(
            Scanner scanner,
            BachecaController controller,
            ComunicazioneView comunicazioneView
    ) {
        this.scanner = scanner;
        this.controller = controller;
        this.comunicazioneView = comunicazioneView;
    }

    // OP4: ricerca per categoria e sottocategorie.
    public void cercaAnnunci() {
        try {
            List<Categoria> categorie = controller.elencaCategorie();
            if (categorie.isEmpty()) {
                System.out.println("Non sono presenti categorie.");
                return;
            }

            System.out.println();
            System.out.println("--- CERCA ANNUNCI ---");
            for (int index = 0; index < categorie.size(); index++) {
                System.out.printf("%d. %s%n",
                        index + 1, categorie.get(index).nome());
            }
            System.out.println("0. Torna al menu");
            System.out.print("Categoria: ");
            int sceltaCategoria = Integer.parseInt(scanner.nextLine().trim());

            if (sceltaCategoria == 0) {
                return;
            }
            if (sceltaCategoria < 1 || sceltaCategoria > categorie.size()) {
                System.out.println("Categoria non valida.");
                return;
            }

            Categoria categoria = categorie.get(sceltaCategoria - 1);
            List<Annuncio> annunci = controller.cercaPerCategoria(
                    categoria.codiceCategoria()
            );
            if (annunci.isEmpty()) {
                System.out.println("Nessun annuncio trovato nella categoria "
                        + "o nelle sue sottocategorie.");
                return;
            }

            Set<Integer> codiciSeguiti = new HashSet<>();
            for (Annuncio seguito : controller.elencaAnnunciSeguiti()) {
                codiciSeguiti.add(seguito.codice());
            }

            for (int index = 0; index < annunci.size(); index++) {
                Annuncio annuncio = annunci.get(index);
                String indicazione = "";
                if (codiciSeguiti.contains(annuncio.codice())) {
                    indicazione = " [già seguito]";
                }
                System.out.printf(
                        "%d. %s | %s | %s euro | autore: %s%s%n",
                        index + 1,
                        annuncio.titolo(),
                        annuncio.nomeCategoria(),
                        annuncio.prezzo(),
                        annuncio.autore(),
                        indicazione
                );
            }

            System.out.println("0. Torna al menu");
            System.out.print("Annuncio: ");
            int sceltaAnnuncio = Integer.parseInt(scanner.nextLine().trim());
            if (sceltaAnnuncio == 0) {
                return;
            }
            if (sceltaAnnuncio < 1 || sceltaAnnuncio > annunci.size()) {
                System.out.println("Annuncio non valido.");
                return;
            }

            Annuncio scelto = annunci.get(sceltaAnnuncio - 1);
            boolean giaSeguito = codiciSeguiti.contains(scelto.codice());

            System.out.println("1. Visualizza dettaglio");
            if (!giaSeguito) {
                System.out.println("2. Segui annuncio");
            }
            System.out.println("0. Torna al menu");
            System.out.print("Operazione: ");
            switch (scanner.nextLine().trim()) {
                case "1" -> mostraDettaglio(scelto, giaSeguito);
                case "2" -> {
                    if (giaSeguito) {
                        System.out.println("Stai già seguendo questo annuncio.");
                    } else {
                        controller.segui(scelto);
                        System.out.println("Ora segui l'annuncio selezionato.");
                    }
                }
                case "0" -> { }
                default -> System.out.println("Operazione non valida.");
            }
        } catch (NumberFormatException exception) {
            System.out.println("Scelta non valida.");
        } catch (IllegalArgumentException exception) {
            System.out.println("Operazione non valida: "
                    + exception.getMessage());
        } catch (SQLException | IllegalStateException exception) {
            System.err.println("Ricerca fallita: " + exception.getMessage());
        }
    }

    public void mostraDettaglio(
            Annuncio annuncio,
            boolean giaSeguito
    ) throws SQLException {
        stampaScheda(annuncio, giaSeguito
                ? "stai seguendo questo annuncio"
                : "non stai seguendo questo annuncio");

        System.out.println();
        if (!annuncio.stato().equals("InVendita")) {
            if (giaSeguito) {
                System.out.println("1. Smetti di seguire");
            }
            System.out.println("0. Torna alla ricerca");
            System.out.print("Operazione: ");
            String operazione = scanner.nextLine().trim();
            if (giaSeguito && operazione.equals("1")) {
                controller.smettiDiSeguire(annuncio);
                System.out.println("Non segui più questo annuncio.");
            } else if (!operazione.equals("0")) {
                System.out.println("Operazione non valida.");
            }
            return;
        }

        System.out.println("1. Inserisci commento");
        System.out.println("2. Invia messaggio privato all'autore");
        System.out.println(giaSeguito
                ? "3. Smetti di seguire"
                : "3. Segui annuncio");
        System.out.println("0. Torna alla ricerca");
        System.out.print("Operazione: ");
        switch (scanner.nextLine().trim()) {
            case "1" -> comunicazioneView.inserisciCommento(annuncio);
            case "2" -> comunicazioneView.inviaMessaggio(annuncio);
            case "3" -> {
                if (giaSeguito) {
                    controller.smettiDiSeguire(annuncio);
                    System.out.println("Non segui più questo annuncio.");
                } else {
                    controller.segui(annuncio);
                    System.out.println("Ora segui questo annuncio.");
                }
            }
            case "0" -> { }
            default -> System.out.println("Operazione non valida.");
        }
    }

    public String mostraDettaglioProprio(
            Annuncio annuncio
    ) throws SQLException {
        stampaScheda(annuncio, "annuncio pubblicato da te");

        System.out.println();
        if (annuncio.stato().equals("InVendita")) {
            System.out.println("1. Inserisci commento");
            System.out.println("2. Modifica annuncio");
            System.out.println("3. Aggiungi nota integrativa");
            System.out.println("4. Contrassegna come venduto");
        }
        System.out.println("0. Torna al menu");
        System.out.print("Operazione: ");
        String operazione = scanner.nextLine().trim();

        if (annuncio.stato().equals("InVendita")
                && operazione.equals("1")) {
            comunicazioneView.inserisciCommento(annuncio);
            return "0";
        }
        if (!annuncio.stato().equals("InVendita")
                && !operazione.equals("0")) {
            return "non_valida";
        }
        return operazione;
    }

    private void stampaScheda(
            Annuncio annuncio,
            String statoMonitoraggio
    ) throws SQLException {
        SchedaAnnuncio scheda = controller.visualizzaDettaglio(
                annuncio.codice()
        );
        DettaglioAnnuncio dettaglio = scheda.dettaglio();

        System.out.println();
        System.out.println("--- DETTAGLIO ANNUNCIO ---");
        System.out.println("Titolo: " + dettaglio.titolo());
        System.out.println("Descrizione: "
                + dettaglio.descrizioneArticolo());
        System.out.println("Prezzo: " + dettaglio.prezzo() + " euro");
        System.out.println("Categoria: " + dettaglio.nomeCategoria());
        System.out.println("Stato: " + dettaglio.stato());
        System.out.println("Autore: " + dettaglio.nomeAutore() + " "
                + dettaglio.cognomeAutore()
                + " (" + dettaglio.autore() + ")");
        System.out.println("Recapito " + dettaglio.recapitoPreferito()
                + ": " + dettaglio.recapitoAutore());
        System.out.println("Monitoraggio: " + statoMonitoraggio);

        System.out.println();
        System.out.println("Note integrative:");
        if (scheda.note().isEmpty()) {
            System.out.println("- Nessuna nota.");
        } else {
            for (NuovaNota nota : scheda.note()) {
                System.out.println("- [" + nota.dataOra() + "] "
                        + nota.testoNota());
            }
        }

        System.out.println();
        System.out.println("Commenti pubblici:");
        if (scheda.commenti().isEmpty()) {
            System.out.println("- Nessun commento.");
        } else {
            for (CommentoPubblico commento : scheda.commenti()) {
                System.out.println("- [" + commento.dataOra() + "] "
                        + commento.autore() + ": " + commento.testo());
            }
        }

    }
}
