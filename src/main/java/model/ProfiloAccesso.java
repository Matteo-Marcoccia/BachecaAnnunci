package model;

public record ProfiloAccesso(
        String username,
        String passwordHash,
        boolean gestore
) {
}
