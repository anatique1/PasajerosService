package com.example.pasajero.services;

import com.example.pasajero.dto.CompletarPerfilGoogleRequest;
import com.example.pasajero.dto.PerfilUsuarioResponse;
import com.example.pasajero.model.CuentaGoogle;
import com.example.pasajero.model.Pasajero;
import com.example.pasajero.repository.CuentaGoogleRepository;
import com.example.pasajero.repository.PasajeroRepository;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

@Service
public class GoogleAuthService {

    private final CuentaGoogleRepository cuentas;
    private final PasajeroRepository pasajeros;
    private final String clientId;

    public GoogleAuthService(CuentaGoogleRepository cuentas,
                             PasajeroRepository pasajeros,
                             @Value("${app.google.client-id:}") String clientId) {
        this.cuentas = cuentas;
        this.pasajeros = pasajeros;
        this.clientId = clientId;
    }

    public PerfilUsuarioResponse iniciarSesion(String credential) {
        GoogleIdToken.Payload claims = verificar(credential);
        CuentaGoogle cuenta = guardarCuenta(claims);
        Pasajero pasajero = buscarOVincularPasajero(cuenta, claims);
        return PerfilUsuarioResponse.desde(cuentas.save(cuenta), pasajero != null && pasajero.validarPasajero());
    }

    public PerfilUsuarioResponse completarPerfil(CompletarPerfilGoogleRequest request) {
        if (request == null || vacio(request.tipoDocumento()) || vacio(request.documento()) || vacio(request.telefono())) {
            throw new IllegalArgumentException("Completa el tipo y número de documento y tu teléfono.");
        }
        GoogleIdToken.Payload claims = verificar(request.credential());
        CuentaGoogle cuenta = guardarCuenta(claims);
        Pasajero pasajero = buscarOVincularPasajero(cuenta, claims);

        String documento = request.documento().trim();
        if (pasajero == null) {
            Pasajero conMismoId = pasajeros.findById(documento).orElse(null);
            if (conMismoId != null && !conMismoId.getCorreo().equalsIgnoreCase(cuenta.getCorreo())) {
                throw new IllegalArgumentException("Ya existe un pasajero con ese documento. Revisa el número ingresado.");
            }
            pasajero = conMismoId != null ? conMismoId : new Pasajero();
            if (pasajero.getId() == null || pasajero.getId().isBlank()) pasajero.setId(documento);
            pasajero.setFechaRegistro(LocalDate.now(ZoneId.of("America/Bogota")).toString());
        }

        String nombre = stringClaim(claims, "given_name");
        String apellido = stringClaim(claims, "family_name");
        if (nombre.isBlank() || apellido.isBlank()) {
            String[] partes = String.valueOf(claims.get("name")).trim().split("\\s+", 2);
            if (nombre.isBlank()) nombre = partes[0];
            if (apellido.isBlank() && partes.length > 1) apellido = partes[1];
        }
        if (apellido.isBlank()) apellido = "Pendiente";

        pasajero.setNombre(nombre);
        pasajero.setApellido(apellido);
        pasajero.setCorreo(cuenta.getCorreo());
        pasajero.setGoogleId(cuenta.getId());
        pasajero.setTipoDocumento(request.tipoDocumento().trim());
        pasajero.setDocumento(documento);
        pasajero.setTelefono(request.telefono().trim());
        if (!pasajero.validarPasajero()) {
            throw new IllegalArgumentException("No se pudo completar el perfil. Verifica los datos e inténtalo de nuevo.");
        }

        Pasajero guardado = pasajeros.save(pasajero);
        cuenta.setPasajeroId(guardado.getId());
        return PerfilUsuarioResponse.desde(cuentas.save(cuenta), true);
    }

    private Pasajero buscarOVincularPasajero(CuentaGoogle cuenta, GoogleIdToken.Payload claims) {
        Pasajero pasajero = cuenta.getPasajeroId() == null
                ? null
                : pasajeros.findById(cuenta.getPasajeroId()).orElse(null);
        if (pasajero == null) {
            pasajero = pasajeros.findByGoogleId(cuenta.getId()).orElse(null);
        }
        if (pasajero == null && !vacio(cuenta.getCorreo())) {
            pasajero = pasajeros.findFirstByCorreoIgnoreCase(cuenta.getCorreo()).orElse(null);
        }
        if (pasajero != null) {
            pasajero.setGoogleId(cuenta.getId());
            pasajero.setCorreo(cuenta.getCorreo());
            String nombre = stringClaim(claims, "given_name");
            String apellido = stringClaim(claims, "family_name");
            if (!nombre.isBlank()) pasajero.setNombre(nombre);
            if (!apellido.isBlank()) pasajero.setApellido(apellido);
            pasajero = pasajeros.save(pasajero);
            cuenta.setPasajeroId(pasajero.getId());
        }
        return pasajero;
    }

    private CuentaGoogle guardarCuenta(GoogleIdToken.Payload claims) {
        String googleId = claims.getSubject();
        CuentaGoogle cuenta = cuentas.findById(googleId).orElseGet(() -> {
            CuentaGoogle nueva = new CuentaGoogle();
            nueva.setId(googleId);
            nueva.setCreadoEn(java.time.Instant.now());
            return nueva;
        });
        cuenta.setCorreo(claims.getEmail());
        cuenta.setNombre(stringClaim(claims, "name"));
        cuenta.setFotoUrl(stringClaim(claims, "picture"));
        cuenta.setUltimoAcceso(java.time.Instant.now());
        return cuentas.save(cuenta);
    }

    private GoogleIdToken.Payload verificar(String credential) {
        if (vacio(credential)) throw new IllegalArgumentException("Google no envió una credencial válida.");
        if (clientId.isBlank()) throw new IllegalStateException("Falta configurar GOOGLE_CLIENT_ID en el backend.");

        try {
            GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(
                    new NetHttpTransport(), GsonFactory.getDefaultInstance())
                    .setAudience(List.of(clientId))
                    .build();
            GoogleIdToken token = verifier.verify(credential);
            if (token == null) throw new IllegalArgumentException("La credencial de Google no es válida o ya venció.");
            GoogleIdToken.Payload claims = token.getPayload();
            if (vacio(claims.getSubject()) || vacio(claims.getEmail())) {
                throw new IllegalArgumentException("La cuenta de Google no contiene un correo o identificador válido.");
            }
            return claims;
        } catch (GeneralSecurityException | IOException e) {
            throw new IllegalStateException("No se pudo verificar la credencial con Google.", e);
        }
    }

    private String stringClaim(GoogleIdToken.Payload claims, String key) {
        Object value = claims.get(key);
        return value == null ? "" : value.toString().trim();
    }

    private boolean vacio(String value) {
        return value == null || value.isBlank();
    }
}
