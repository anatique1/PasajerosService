package com.example.pasajero.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class Pasajero extends Persona {

    private String fechaRegistro;

    public boolean validarPasajero() {
        return getNombre() != null && !getNombre().isBlank() &&
                getApellido() != null && !getApellido().isBlank() &&
                getDocumento() != null && !getDocumento().isBlank() &&
                getTipoDocumento() != null && !getTipoDocumento().isBlank() &&
                getTelefono() != null && !getTelefono().isBlank() &&
                getCorreo() != null && !getCorreo().isBlank() &&
                getId() != null && !getId().isBlank() &&
                getFechaRegistro() != null && !getFechaRegistro().isBlank();
    }
}