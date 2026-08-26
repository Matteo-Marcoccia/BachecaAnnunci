package model;

import java.time.LocalDateTime;

public record NuovaNota(
        int codiceNota,
        String testoNota,
        LocalDateTime dataOra,
        int codiceAnnuncio
) {
}
