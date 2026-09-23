package com.fullprojectstudio.backend.model;

public enum StatoPagamento {
    PAGATO,
    // Non più usati dall'interfaccia (un pagamento registrato è sempre effettuato): restano
    // solo perché eventuali pagamenti già salvati con questi stati continuino a essere letti.
    IN_SOSPESO,
    RIMBORSATO
}
