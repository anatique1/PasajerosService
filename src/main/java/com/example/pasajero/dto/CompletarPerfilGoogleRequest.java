package com.example.pasajero.dto;

public record CompletarPerfilGoogleRequest(
        String credential,
        String tipoDocumento,
        String documento,
        String telefono
) {
}
