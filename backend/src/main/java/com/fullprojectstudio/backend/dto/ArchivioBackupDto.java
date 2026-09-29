package com.fullprojectstudio.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ArchivioBackupDto {
    private String cartella;
    private String orarioAutomatico;
    private List<FileBackupDto> file;
}
