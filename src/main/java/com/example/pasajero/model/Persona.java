package com.example.pasajero.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.springframework.data.annotation.Id;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public abstract class Persona {

    @Id
    @Schema(description = "Identificador. Opcional al crear: si no se envía, MongoDB lo genera.",
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
}
