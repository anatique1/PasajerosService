package com.example.pasajero.controller;

import com.example.pasajero.dto.GoogleLoginRequest;
import com.example.pasajero.dto.PerfilUsuarioResponse;
import com.example.pasajero.services.GoogleAuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final GoogleAuthService googleAuthService;

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
