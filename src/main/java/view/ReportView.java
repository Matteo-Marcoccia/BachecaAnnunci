package view;

import controller.ReportController;
import model.StatisticaUtente;

import java.sql.SQLException;
import java.util.List;

public final class ReportView {

    private final ReportController controller;

    public ReportView(ReportController controller) {
        this.controller = controller;
    }

    public void mostraReport() {
        try {
            List<StatisticaUtente> statistiche = controller.generaReport();
            System.out.println();
            System.out.println("--- REPORT STATISTICO UTENTI ---");
            System.out.printf("%-25s %10s %10s %12s%n",
                    "Username", "Totali", "Venduti", "Percentuale");
            System.out.println("-".repeat(61));

            for (StatisticaUtente statistica : statistiche) {
                System.out.printf("%-25s %10d %10d %11s%%%n",
                        statistica.username(),
                        statistica.annunciTotali(),
                        statistica.annunciVenduti(),
                        statistica.percentualeVenduti());
            }
        } catch (SQLException | IllegalStateException exception) {
            System.err.println("Generazione del report fallita: "
                    + exception.getMessage());
        }
    }
}
