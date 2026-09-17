package com.fullprojectstudio.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PresenzaDto {
    private Long id;
    private Long iscrizioneId;
    private String studenteNomeCompleto;
    private LocalDate dataLezione;
    private boolean presente;
    private String note;
}
