package controller;

import dao.AnnuncioDAO;
import dao.CategoriaDAO;
import dao.NuovaNotaDAO;
import model.Annuncio;
import model.Categoria;
import model.DestinatarioNotifica;
import model.RuoloUtente;
import sessione.SessionManager;
import sessione.SessioneUtente;
import service.NotificaService;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;

public final class GestioneAnnunciController {

    private final AnnuncioDAO annuncioDAO;
    private final CategoriaDAO categoriaDAO;
    private final NuovaNotaDAO nuovaNotaDAO;
    private final SessionManager sessionManager;
    private final NotificaService notificaService;

    public GestioneAnnunciController() {
        this.annuncioDAO = new AnnuncioDAO();
        this.categoriaDAO = new CategoriaDAO();
        this.nuovaNotaDAO = new NuovaNotaDAO();
        this.sessionManager = SessionManager.fornisciIstanza();
        this.notificaService = new NotificaService();
    }

    public List<Categoria> elencaCategorie() throws SQLException {
        sessionManager.verificaRuolo(RuoloUtente.UTENTE);
        return categoriaDAO.elenca(sessionManager.fornisciSessioneValida());
    }

    // OP2: pubblicazione di un annuncio.
    public void pubblicaAnnuncio(
            String titolo,
            String descrizione,
            BigDecimal prezzo,
            int codiceCategoria
    ) throws SQLException {
        sessionManager.verificaRuolo(RuoloUtente.UTENTE);

        if (titolo.isBlank() || descrizione.isBlank()) {
            throw new IllegalArgumentException(
                    "titolo e descrizione non possono essere vuoti."
            );
        }
        if (prezzo.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException(
                    "il prezzo deve essere maggiore di zero."
            );
        }

        SessioneUtente sessione =
                sessionManager.fornisciSessioneValida();

        annuncioDAO.pubblica(
                sessione,
                titolo.trim(),
                descrizione.trim(),
                prezzo,
                codiceCategoria
        );
    }

    // OP3: pubblicazione di una nota integrativa.
    public int pubblicaNota(
            int codiceAnnuncio,
            String testo
    ) throws SQLException {
        sessionManager.verificaRuolo(RuoloUtente.UTENTE);
        if (testo == null || testo.isBlank()) {
            throw new IllegalArgumentException(
                    "il testo della nota non può essere vuoto."
            );
        }

        List<DestinatarioNotifica> destinatari = nuovaNotaDAO.pubblica(
                sessionManager.fornisciSessioneValida(),
                codiceAnnuncio,
                testo.trim()
        );
        notificaService.inviaSimulate(
                destinatari,
                "È stata pubblicata una nuova nota sull'annuncio seguito."
        );
        return destinatari.size();
    }

    // OP9: modifica di un proprio annuncio.
    public int modificaAnnuncio(
            int codiceAnnuncio,
            String titolo,
            String descrizione,
            BigDecimal prezzo,
            int codiceCategoria
    ) throws SQLException {
        sessionManager.verificaRuolo(RuoloUtente.UTENTE);

        if (titolo.isBlank() || descrizione.isBlank()) {
            throw new IllegalArgumentException(
                    "titolo e descrizione non possono essere vuoti."
            );
        }
        if (prezzo.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException(
                    "il prezzo deve essere maggiore di zero."
            );
        }

        List<DestinatarioNotifica> destinatari = annuncioDAO.modifica(
                sessionManager.fornisciSessioneValida(),
                codiceAnnuncio,
                titolo.trim(),
                descrizione.trim(),
                prezzo,
                codiceCategoria
        );
        notificaService.inviaSimulate(
                destinatari,
                "Sono state modificate le informazioni dell'annuncio seguito."
        );
        return destinatari.size();
    }

    // OP10: aggiornamento dello stato a Venduto.
    public int contrassegnaVenduto(
            int codiceAnnuncio
    ) throws SQLException {
        sessionManager.verificaRuolo(RuoloUtente.UTENTE);
        List<DestinatarioNotifica> destinatari =
                annuncioDAO.contrassegnaVenduto(
                        sessionManager.fornisciSessioneValida(),
                        codiceAnnuncio
                );
        notificaService.inviaSimulate(
                destinatari,
                "L'annuncio seguito è stato contrassegnato come venduto."
        );
        return destinatari.size();
    }

    // OP14: visualizzazione dei propri annunci.
    public List<Annuncio> elencaPropriAnnunci() throws SQLException {
        sessionManager.verificaRuolo(RuoloUtente.UTENTE);
        return annuncioDAO.elencaPerUtente(
                sessionManager.fornisciSessioneValida()
        );
    }
}
