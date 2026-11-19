package com.example.pasajero.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Document(collection = "pasajeros")
public class Pasajero extends Persona {

    @Schema(description = "Formato AAAA-MM-DD", example = "2026-11-18")
    private String fechaRegistro;

    @JsonIgnore
    private String googleId;

    public Pasajero(String fechaRegistro) {
        this.fechaRegistro = fechaRegistro;
    }

    /** El id es opcional: si no llega, MongoDB lo genera al guardar. */
    public boolean validarPasajero() {
        return getNombre() != null && !getNombre().isBlank() &&
                getApellido() != null && !getApellido().isBlank() &&
                getDocumento() != null && !getDocumento().isBlank() &&
                getTipoDocumento() != null && !getTipoDocumento().isBlank() &&
                getTelefono() != null && !getTelefono().isBlank() &&
                getCorreo() != null && !getCorreo().isBlank() &&
                getFechaRegistro() != null && !getFechaRegistro().isBlank();
    }
}
