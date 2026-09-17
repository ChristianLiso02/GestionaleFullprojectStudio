package com.fullprojectstudio.frontend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SalaDto {
    private Long id;
    private String nome;
    private Integer capienza;
    private String note;
}
