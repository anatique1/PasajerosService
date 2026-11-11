package com.example.pasajero.services;

import com.example.pasajero.dto.PerfilUsuarioResponse;
import com.example.pasajero.model.CuentaGoogle;
import com.example.pasajero.repository.CuentaGoogleRepository;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.time.Instant;
import java.util.List;

@Service
public class GoogleAuthService {

    private final CuentaGoogleRepository repository;
    private final String clientId;

    public GoogleAuthService(CuentaGoogleRepository repository,
                             @Value("${app.google.client-id:}") String clientId) {
        this.repository = repository;
        this.clientId = clientId;
    }

    public PerfilUsuarioResponse iniciarSesion(String credential) {
        if (credential == null || credential.isBlank()) {
            throw new IllegalArgumentException("Google no envió una credencial válida.");
        }
        if (clientId.isBlank()) {
            throw new IllegalStateException("Falta configurar GOOGLE_CLIENT_ID en el backend.");
        }

        try {
            GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(
                    new NetHttpTransport(), GsonFactory.getDefaultInstance())
                    .setAudience(List.of(clientId))
                    .build();
            GoogleIdToken token = verifier.verify(credential);
            if (token == null) {
                throw new IllegalArgumentException("La credencial de Google no es válida o ya venció.");
            }

            GoogleIdToken.Payload claims = token.getPayload();
            String googleId = claims.getSubject();
            if (googleId == null || googleId.isBlank()) {
                throw new IllegalArgumentException("La credencial no contiene un identificador de cuenta.");
            }

            Instant ahora = Instant.now();
            CuentaGoogle cuenta = repository.findById(googleId).orElseGet(() -> {
                CuentaGoogle nueva = new CuentaGoogle();
                nueva.setId(googleId);
                nueva.setCreadoEn(ahora);
                return nueva;
            });
            cuenta.setCorreo(claims.getEmail());
            cuenta.setNombre((String) claims.get("name"));
            cuenta.setFotoUrl((String) claims.get("picture"));
            cuenta.setUltimoAcceso(ahora);
            return PerfilUsuarioResponse.desde(repository.save(cuenta));
        } catch (GeneralSecurityException | IOException e) {
            throw new IllegalStateException("No se pudo verificar la credencial con Google.", e);
        }
    }
}
