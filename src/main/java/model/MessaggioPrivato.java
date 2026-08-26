package model;

import java.time.LocalDateTime;

public record MessaggioPrivato(
        int codiceMessaggio,
        String testo,
        LocalDateTime dataOra,
        int codiceAnnuncio,
        String mittente,
        String destinatario
) {
}
