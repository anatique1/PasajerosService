package com.example.pasajero.controller;

import com.example.pasajero.model.Pasajero;
import com.example.pasajero.services.PasajeroService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/pasajeros")
@RequiredArgsConstructor
public class PasajeroController {

    private final PasajeroService pasajeroService;

    @GetMapping("/conexion")
    public String conexion() {
        return "Servicio de pasajeros activo";
    }

    @PostMapping
    public ResponseEntity<?> crear(@RequestBody Pasajero pasajero) {
        try {
            Pasajero creado = pasajeroService.crearPasajero(pasajero);
            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(Map.of(
                            "mensaje", "Pasajero creado correctamente.",
                            "pasajero", creado
                    ));
        } catch (RuntimeException e) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("mensaje", e.getMessage()));
        }
    }

    @GetMapping
    public ResponseEntity<?> listar() {
        List<Pasajero> pasajeros = pasajeroService.listarPasajeros();

        String mensaje = pasajeros.isEmpty()
                ? "No hay pasajeros registrados."
                : "Pasajeros consultados correctamente.";

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(Map.of(
                        "mensaje", mensaje,
                        "pasajeros", pasajeros
                ));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> buscarPorId(@PathVariable String id) {
        Pasajero pasajero = pasajeroService.buscarPasajero(id);

        if (pasajero == null) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(Map.of("mensaje", "No se encontró un pasajero con el ID " + id));
        }

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(Map.of(
                        "mensaje", "Pasajero encontrado correctamente.",
                        "pasajero", pasajero
                ));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> actualizar(@PathVariable String id, @RequestBody Pasajero pasajero) {
        Pasajero existente = pasajeroService.buscarPasajero(id);

        if (existente == null) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(Map.of("mensaje", "No se encontró un pasajero con el ID " + id));
        }

        try {
            pasajero.setId(id);
            pasajeroService.modificarPasajero(pasajero);

            return ResponseEntity
                    .status(HttpStatus.OK)
                    .body(Map.of(
                            "mensaje", "Pasajero actualizado correctamente.",
                            "pasajero", pasajero
                    ));
        } catch (RuntimeException e) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("mensaje", e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminar(@PathVariable String id) {
        try {
            pasajeroService.eliminarPasajero(id);
            return ResponseEntity
                    .status(HttpStatus.OK)
                    .body(Map.of("mensaje", "Pasajero eliminado correctamente."));
        } catch (RuntimeException e) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(Map.of("mensaje", e.getMessage()));
        }
    }
}