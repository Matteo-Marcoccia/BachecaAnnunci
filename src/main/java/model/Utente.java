package model;

import java.time.LocalDate;

public record Utente(
        String username,
        String nome,
        String cognome,
        String codiceFiscale,
        LocalDate dataNascita,
        String indirizzoResidenza,
        String indirizzoFatturazione,
        String email,
        String cellulare,
        String telefonoFisso,
        String recapitoPreferito
) {
}
