package model;

import java.math.BigDecimal;

public record Annuncio(
        int codice,
        String titolo,
        String descrizioneArticolo,
        BigDecimal prezzo,
        String stato,
        String autore,
        int codiceCategoria,
        String nomeCategoria
) {
}
