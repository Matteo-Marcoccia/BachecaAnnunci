package model;

import java.math.BigDecimal;

public record DettaglioAnnuncio(
        int codice,
        String titolo,
        String descrizioneArticolo,
        BigDecimal prezzo,
        String stato,
        String autore,
        int codiceCategoria,
        String nomeCategoria,
        String nomeAutore,
        String cognomeAutore,
        String recapitoPreferito,
        String recapitoAutore
) {
}
