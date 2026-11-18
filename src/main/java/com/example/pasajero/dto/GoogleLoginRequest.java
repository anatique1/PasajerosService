package com.example.pasajero.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record GoogleLoginRequest(
        @Schema(description = "ID token (JWT) entregado por Google Identity Services", example = "eyJhbGciOiJSUzI1NiIs...")
        String credential) {
}
