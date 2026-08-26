package model;

import java.util.List;

public record SchedaAnnuncio(
        DettaglioAnnuncio dettaglio,
        List<NuovaNota> note,
        List<CommentoPubblico> commenti
) {
}
