package com.RutaMacht.RutaMacht.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PasajeroDTO {
    @Schema(description = "Identificador. Opcional al crear: si no se envía, el microservicio de pasajeros lo genera.",
            example = "665f1c2e9b1e8a3d4c5b6a7f")
    private String id;
    @Schema(example = "Laura")
    private String nombre;
    @Schema(example = "Gómez")
    private String apellido;
    @Schema(example = "1234567890")
    private String documento;
    @Schema(example = "Cédula de ciudadanía")
    private String tipoDocumento;
    @Schema(example = "3001234567")
    private String telefono;
    @Schema(example = "laura@correo.com")
    private String correo;
    @Schema(description = "Formato AAAA-MM-DD", example = "2026-11-18")
    private String fechaRegistro;
}
