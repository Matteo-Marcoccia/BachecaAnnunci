package view;

import controller.GestioneCategorieController;
import model.Categoria;

import java.sql.SQLException;
import java.util.List;
import java.util.Scanner;

public final class CategoriaView {

    private final Scanner scanner;
    private final GestioneCategorieController controller;

    public CategoriaView(
            Scanner scanner,
            GestioneCategorieController controller
    ) {
        this.scanner = scanner;
        this.controller = controller;
    }

    // OP11: creazione e collocazione di una categoria.
    public void creaCategoria() {
        try {
            System.out.print("Nome della nuova categoria: ");
            String nome = scanner.nextLine();

            List<Categoria> categorie = controller.elencaCategorie();
            System.out.println("0. Nessuna categoria padre (radice)");
            for (int index = 0; index < categorie.size(); index++) {
                System.out.printf("%d. %s%n",
                        index + 1,
                        categorie.get(index).nome());
            }
            System.out.print("Categoria padre: ");
            int scelta = Integer.parseInt(scanner.nextLine().trim());

            Integer categoriaPadre = null;
            if (scelta != 0) {
                if (scelta < 1 || scelta > categorie.size()) {
                    System.out.println("Categoria padre non valida.");
                    return;
                }
                categoriaPadre = categorie
                        .get(scelta - 1)
                        .codiceCategoria();
            }

            controller.creaCategoria(nome, categoriaPadre);
            System.out.println("Categoria creata correttamente.");
        } catch (NumberFormatException exception) {
            System.out.println("Scelta non valida.");
        } catch (IllegalArgumentException exception) {
            System.out.println("Dati non validi: " + exception.getMessage());
        } catch (SQLException | IllegalStateException exception) {
            System.err.println("Creazione della categoria fallita: "
                    + exception.getMessage());
        }
    }
}
