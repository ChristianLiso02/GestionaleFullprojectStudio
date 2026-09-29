package com.fullprojectstudio.backend.model;

public enum TipoPagamento {
    // Quota di un'iscrizione (mensile, trimestrale...), oppure un incasso libero senza iscrizione.
    QUOTA_CORSO,
    // Singola lezione di un corso pagata da chi non è iscritto: non crea iscrizioni né quote.
    LEZIONE_SINGOLA
}
