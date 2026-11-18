package com.example.pasajero.controller;

import com.example.pasajero.model.Pasajero;
import com.example.pasajero.services.PasajeroService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Tag(name = "Pasajeros", description = "CRUD de pasajeros (persistidos en MongoDB)")
@RestController
@RequestMapping("/api/pasajeros")
@RequiredArgsConstructor
public class PasajeroController {

    private final PasajeroService pasajeroService;

    @Operation(summary = "Verificar conexión",
            description = "Lo usa APIRutaMacht (GET /api/conexion/pasajero) para comprobar que este servicio está activo.")
    @ApiResponse(responseCode = "200", description = "Servicio activo")
    @GetMapping("/conexion")
    public String conexion() {
        return "Servicio de pasajeros activo";
    }

    @Operation(summary = "Crear pasajero",
            description = "Registra un pasajero. El campo id es opcional: si no se envía, MongoDB lo genera.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Pasajero creado"),
            @ApiResponse(responseCode = "400", description = "Datos incompletos o pasajero duplicado")
    })
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

    @Operation(summary = "Listar pasajeros",
            description = "Devuelve todos los pasajeros almacenados en MongoDB.")
    @ApiResponse(responseCode = "200", description = "Lista de pasajeros (puede estar vacía)")
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

    @Operation(summary = "Consultar pasajero por id")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Pasajero encontrado"),
            @ApiResponse(responseCode = "404", description = "No existe un pasajero con ese id")
    })
    @GetMapping("/{id}")
    public ResponseEntity<?> buscarPorId(
            @Parameter(description = "Id del pasajero") @PathVariable String id) {
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

    @Operation(summary = "Actualizar pasajero",
            description = "Reemplaza los datos del pasajero indicado conservando su id. Todos los campos son obligatorios salvo el id.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Pasajero actualizado"),
            @ApiResponse(responseCode = "400", description = "Datos incompletos"),
            @ApiResponse(responseCode = "404", description = "No existe un pasajero con ese id")
    })
    @PutMapping("/{id}")
    public ResponseEntity<?> actualizar(
            @Parameter(description = "Id del pasajero") @PathVariable String id,
            @RequestBody Pasajero pasajero) {
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

    @Operation(summary = "Eliminar pasajero",
            description = "Elimina el pasajero de MongoDB.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Pasajero eliminado"),
            @ApiResponse(responseCode = "404", description = "No existe un pasajero con ese id")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminar(
            @Parameter(description = "Id del pasajero") @PathVariable String id) {
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
