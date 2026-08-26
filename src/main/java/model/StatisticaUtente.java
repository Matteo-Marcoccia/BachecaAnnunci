package model;

import java.math.BigDecimal;

public record StatisticaUtente(
        String username,
        int annunciTotali,
        int annunciVenduti,
        BigDecimal percentualeVenduti
) {
}
