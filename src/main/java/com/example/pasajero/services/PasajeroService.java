package com.example.pasajero.services;

import com.example.pasajero.model.Pasajero;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class PasajeroService {

    private final Map<String, Pasajero> pasajeros = new ConcurrentHashMap<>();

    public Pasajero crearPasajero(Pasajero pasajero) {
        if (!pasajero.validarPasajero()) {
            throw new RuntimeException("Los datos del pasajero están incompletos.");
        }
        if (pasajeros.containsKey(pasajero.getId())) {
            throw new RuntimeException("Ya existe un pasajero con el ID " + pasajero.getId());
        }
        pasajeros.put(pasajero.getId(), pasajero);
        return pasajero;
    }

    public List<Pasajero> listarPasajeros() {
        return new ArrayList<>(pasajeros.values());
    }

    public Pasajero buscarPasajero(String id) {
        return pasajeros.get(id);
    }

    public Pasajero modificarPasajero(Pasajero pasajero) {
        if (!pasajeros.containsKey(pasajero.getId())) {
            throw new RuntimeException("No se encontró un pasajero con el ID " + pasajero.getId());
        }
        if (!pasajero.validarPasajero()) {
            throw new RuntimeException("Los datos del pasajero están incompletos.");
        }
        pasajeros.put(pasajero.getId(), pasajero);
        return pasajero;
    }

    public void eliminarPasajero(String id) {
        if (pasajeros.remove(id) == null) {
            throw new RuntimeException("No se encontró un pasajero con el ID " + id);
        }
    }
}