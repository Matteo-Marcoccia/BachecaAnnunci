package service;

import model.DestinatarioNotifica;

import java.util.List;

public final class NotificaService {

    private static final String ROSSO = "\u001B[31m";
    private static final String RESET_COLORE = "\u001B[0m";

    public void inviaSimulate(
            List<DestinatarioNotifica> destinatari,
            String messaggio
    ) {
        for (DestinatarioNotifica destinatario : destinatari) {
            System.out.printf(
                    "%n%s[NOTIFICA SIMULATA]%n"
                            + "Canale: %s%n"
                            + "Destinatario: %s%n"
                            + "Recapito: %s%n"
                            + "Messaggio: %s%s%n",
                    ROSSO,
                    destinatario.recapitoPreferito(),
                    destinatario.username(),
                    destinatario.recapito(),
                    messaggio,
                    RESET_COLORE
            );
        }
        if (!destinatari.isEmpty()) {
            System.out.println();
        }
    }
}
