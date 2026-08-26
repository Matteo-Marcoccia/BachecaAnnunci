package model;

import java.time.LocalDateTime;

public record CommentoPubblico(
        int codiceCommento,
        String testo,
        LocalDateTime dataOra,
        int codiceAnnuncio,
        String autore
) {
}
