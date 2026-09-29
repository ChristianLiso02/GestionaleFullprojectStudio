package com.fullprojectstudio.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FileBackupDto {
    /** EXCEL (da aprire per consultare) oppure DATABASE (copia completa per ripristinare). */
    private String tipo;
    private String nome;
    private LocalDateTime data;
    private long dimensione;
}
