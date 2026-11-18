package com.example.pasajero.controller;

import com.example.pasajero.dto.GoogleLoginRequest;
import com.example.pasajero.dto.PerfilUsuarioResponse;
import com.example.pasajero.services.GoogleAuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Tag(name = "Autenticación", description = "Inicio de sesión con Google (guarda la cuenta en MongoDB)")
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final GoogleAuthService googleAuthService;

    @Operation(summary = "Iniciar sesión con Google",
            description = "Valida el ID token de Google y crea o actualiza la cuenta del usuario en la colección cuentas.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Inicio de sesión correcto"),
            @ApiResponse(responseCode = "401", description = "Credencial ausente, inválida o vencida"),
            @ApiResponse(responseCode = "503", description = "Falta GOOGLE_CLIENT_ID o no se pudo verificar con Google")
    })
    @PostMapping("/google")
    public ResponseEntity<?> loginGoogle(@RequestBody GoogleLoginRequest request) {
        try {
            PerfilUsuarioResponse perfil = googleAuthService.iniciarSesion(request.credential());
            return ResponseEntity.ok(Map.of("mensaje", "Inicio de sesión correcto.", "usuario", perfil));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("mensaje", e.getMessage()));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(Map.of("mensaje", e.getMessage()));
        }
    }
}
