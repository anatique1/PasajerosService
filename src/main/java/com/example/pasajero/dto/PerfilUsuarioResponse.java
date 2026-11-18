package com.example.pasajero.dto;

import com.example.pasajero.model.CuentaGoogle;

public record PerfilUsuarioResponse(String id, String correo, String nombre, String fotoUrl, String rol) {
    public static PerfilUsuarioResponse desde(CuentaGoogle cuenta) {
        return new PerfilUsuarioResponse(cuenta.getId(), cuenta.getCorreo(), cuenta.getNombre(), cuenta.getFotoUrl(), cuenta.getRol());
    }
}
