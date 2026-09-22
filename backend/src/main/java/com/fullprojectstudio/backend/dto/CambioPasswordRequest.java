package com.fullprojectstudio.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CambioPasswordRequest {

    @NotBlank(message = "La password attuale è obbligatoria")
    private String vecchiaPassword;

    @NotBlank(message = "La nuova password è obbligatoria")
    @Size(min = 8, message = "La nuova password deve avere almeno 8 caratteri")
    private String nuovaPassword;
}
