package view;

import controller.GestioneAnnunciController;
import model.Annuncio;
import model.Categoria;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;
import java.util.Scanner;

public final class AnnuncioView {

    private final Scanner scanner;
    private final GestioneAnnunciController controller;
    private final BachecaView bachecaView;

    public AnnuncioView(
            Scanner scanner,
            GestioneAnnunciController controller,
            BachecaView bachecaView
    ) {
        this.scanner = scanner;
        this.controller = controller;
        this.bachecaView = bachecaView;
    }

    // OP2: pubblicazione di un annuncio.
    public void pubblicaAnnuncio() {
        try {
            List<Categoria> categorie = controller.elencaCategorie();

            if (categorie.isEmpty()) {
                System.out.println("Non sono presenti categorie.");
                return;
            }

            System.out.println();
            System.out.println("--- CATEGORIE DISPONIBILI ---");
            for (int index = 0; index < categorie.size(); index++) {
                System.out.printf(
                        "%d. %s%n",
                        index + 1,
                        categorie.get(index).nome()
                );
            }

            System.out.print("Scelta categoria: ");
            int sceltaCategoria = Integer.parseInt(
                    scanner.nextLine().trim()
            );

            if (sceltaCategoria < 1
                    || sceltaCategoria > categorie.size()) {
                System.out.println("Categoria non valida.");
                return;
            }

            int codiceCategoria = categorie
                    .get(sceltaCategoria - 1)
                    .codiceCategoria();

            System.out.print("Titolo: ");
            String titolo = scanner.nextLine();

            System.out.print("Descrizione: ");
            String descrizione = scanner.nextLine();

            System.out.print("Prezzo: ");
            BigDecimal prezzo = new BigDecimal(
                    scanner.nextLine().trim().replace(',', '.')
            );

            controller.pubblicaAnnuncio(
                    titolo,
                    descrizione,
                    prezzo,
                    codiceCategoria
            );

            System.out.println("Annuncio pubblicato correttamente.");
        } catch (NumberFormatException exception) {
            System.out.println("Scelta della categoria o prezzo non valido.");
        } catch (IllegalArgumentException exception) {
            System.out.println("Dati non validi: " + exception.getMessage());
        } catch (IllegalStateException | SQLException exception) {
            System.err.println("Pubblicazione fallita: "
                    + exception.getMessage());
        }
    }

    public void gestisciPropriAnnunci() {
        try {
            List<Annuncio> annunci = controller.elencaPropriAnnunci();
            if (annunci.isEmpty()) {
                System.out.println("Non hai ancora pubblicato annunci.");
                return;
            }

            stampaElencoAnnunci(annunci);
            System.out.println("0. Torna al menu");
            System.out.print("Annuncio da gestire: ");
            int scelta = Integer.parseInt(scanner.nextLine().trim());

            if (scelta == 0) {
                return;
            }
            if (scelta < 1 || scelta > annunci.size()) {
                System.out.println("Annuncio non valido.");
                return;
            }

            Annuncio annuncio = annunci.get(scelta - 1);
            switch (bachecaView.mostraDettaglioProprio(annuncio)) {
                case "2" -> modificaAnnuncio(annuncio);
                case "3" -> pubblicaNota(annuncio);
                case "4" -> contrassegnaVenduto(annuncio);
                case "0" -> { }
                default -> System.out.println("Operazione non valida.");
            }
        } catch (NumberFormatException exception) {
            System.out.println("Scelta non valida.");
        } catch (IllegalStateException | SQLException exception) {
            System.err.println("Caricamento degli annunci fallito: "
                    + exception.getMessage());
        }
    }

    private void contrassegnaVenduto(Annuncio annuncio) {
        try {
            System.out.print(
                    "Confermi la vendita dell'annuncio? (s/n): "
            );
            if (!scanner.nextLine().trim().equalsIgnoreCase("s")) {
                System.out.println("Operazione annullata.");
                return;
            }

            int destinatari = controller.contrassegnaVenduto(
                    annuncio.codice()
            );
            System.out.println("Annuncio contrassegnato come venduto.");
            System.out.println("Follower da notificare: " + destinatari);
            attendiInvioSeNecessario(destinatari);
        } catch (IllegalStateException | SQLException exception) {
            System.err.println("Aggiornamento dello stato fallito: "
                    + exception.getMessage());
        }
    }

    private void pubblicaNota(Annuncio annuncio) {
        try {
            System.out.print("Testo della nota: ");
            String testo = scanner.nextLine();

            int destinatari = controller.pubblicaNota(
                    annuncio.codice(),
                    testo
            );
            System.out.println("Nota pubblicata correttamente.");
            System.out.println("Follower da notificare: " + destinatari);
            attendiInvioSeNecessario(destinatari);
        } catch (IllegalArgumentException exception) {
            System.out.println("Dati non validi: " + exception.getMessage());
        } catch (IllegalStateException | SQLException exception) {
            System.err.println("Pubblicazione della nota fallita: "
                    + exception.getMessage());
        }
    }

    private void modificaAnnuncio(Annuncio annuncio) {
        try {
            System.out.print("Nuovo titolo [" + annuncio.titolo() + "]: ");
            String titolo = scanner.nextLine();
            if (titolo.isBlank()) {
                titolo = annuncio.titolo();
            }

            System.out.print("Nuova descrizione ["
                    + annuncio.descrizioneArticolo() + "]: ");
            String descrizione = scanner.nextLine();
            if (descrizione.isBlank()) {
                descrizione = annuncio.descrizioneArticolo();
            }

            System.out.print("Nuovo prezzo [" + annuncio.prezzo() + "]: ");
            String prezzoInserito = scanner.nextLine().trim();
            BigDecimal prezzo = prezzoInserito.isEmpty()
                    ? annuncio.prezzo()
                    : new BigDecimal(prezzoInserito.replace(',', '.'));

            List<Categoria> categorie = controller.elencaCategorie();
            System.out.println("0. Mantieni " + annuncio.nomeCategoria());
            for (int index = 0; index < categorie.size(); index++) {
                System.out.printf("%d. %s%n", index + 1,
                        categorie.get(index).nome());
            }
            System.out.print("Nuova categoria: ");
            int sceltaCategoria = Integer.parseInt(
                    scanner.nextLine().trim()
            );

            int codiceCategoria = annuncio.codiceCategoria();
            if (sceltaCategoria != 0) {
                if (sceltaCategoria < 1
                        || sceltaCategoria > categorie.size()) {
                    System.out.println("Categoria non valida.");
                    return;
                }
                codiceCategoria = categorie
                        .get(sceltaCategoria - 1)
                        .codiceCategoria();
            }

            int destinatari = controller.modificaAnnuncio(
                    annuncio.codice(),
                    titolo,
                    descrizione,
                    prezzo,
                    codiceCategoria
            );
            System.out.println("Annuncio modificato correttamente.");
            System.out.println("Follower da notificare: " + destinatari);
            attendiInvioSeNecessario(destinatari);
        } catch (NumberFormatException exception) {
            System.out.println("Prezzo o categoria non validi.");
        } catch (IllegalArgumentException exception) {
            System.out.println("Dati non validi: " + exception.getMessage());
        } catch (IllegalStateException | SQLException exception) {
            System.err.println("Modifica fallita: "
                    + exception.getMessage());
        }
    }

    private void stampaElencoAnnunci(List<Annuncio> annunci) {
        System.out.println();
        System.out.println("--- I MIEI ANNUNCI ---");
        for (int index = 0; index < annunci.size(); index++) {
            Annuncio annuncio = annunci.get(index);
            System.out.printf(
                    "%d. %s | %s | %s euro | %s%n",
                    index + 1,
                    annuncio.titolo(),
                    annuncio.nomeCategoria(),
                    annuncio.prezzo(),
                    annuncio.stato()
            );
        }
    }

    private void attendiInvioSeNecessario(int destinatari) {
        if (destinatari > 0) {
            System.out.print("Premi Invio per continuare...");
            scanner.nextLine();
        }
    }

    // Qui raccoglieremo e mostreremo i dati relativi a OP3-OP5 e OP9-OP10.
}
