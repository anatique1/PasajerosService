package com.example.pasajero.repository;

import com.example.pasajero.model.Pasajero;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface PasajeroRepository extends MongoRepository<Pasajero, String> {
    Optional<Pasajero> findByGoogleId(String googleId);
    Optional<Pasajero> findFirstByCorreoIgnoreCase(String correo);
}
