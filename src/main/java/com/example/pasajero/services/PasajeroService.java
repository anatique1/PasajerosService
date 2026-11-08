package com.example.pasajero.services;

import com.example.pasajero.model.Pasajero;
import com.example.pasajero.repository.PasajeroRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PasajeroService {

    private final PasajeroRepository repository;

    public Pasajero crearPasajero(Pasajero pasajero) {
        if (!pasajero.validarPasajero()) {
            throw new RuntimeException("Los datos del pasajero están incompletos.");
        }
        if (repository.existsById(pasajero.getId())) {
            throw new RuntimeException("Ya existe un pasajero con el ID " + pasajero.getId());
        }
        return repository.save(pasajero);
    }

    public List<Pasajero> listarPasajeros() {
        return repository.findAll();
    }

    public Pasajero buscarPasajero(String id) {
        return repository.findById(id).orElse(null);
    }

    public Pasajero modificarPasajero(Pasajero pasajero) {
        if (!repository.existsById(pasajero.getId())) {
            throw new RuntimeException("No se encontró un pasajero con el ID " + pasajero.getId());
        }
        if (!pasajero.validarPasajero()) {
            throw new RuntimeException("Los datos del pasajero están incompletos.");
        }
        return repository.save(pasajero);
    }

    public void eliminarPasajero(String id) {
        if (!repository.existsById(id)) {
            throw new RuntimeException("No se encontró un pasajero con el ID " + id);
        }
        repository.deleteById(id);
    }
}
