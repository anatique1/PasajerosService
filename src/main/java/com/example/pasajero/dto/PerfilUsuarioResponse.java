package com.example.pasajero.dto;

import com.example.pasajero.model.CuentaGoogle;

public record PerfilUsuarioResponse(String id, String correo, String nombre, String fotoUrl, String rol, boolean perfilCompleto) {
    public static PerfilUsuarioResponse desde(CuentaGoogle cuenta, boolean perfilCompleto) {
        return new PerfilUsuarioResponse(cuenta.getId(), cuenta.getCorreo(), cuenta.getNombre(), cuenta.getFotoUrl(), cuenta.getRol(), perfilCompleto);
    }
}
