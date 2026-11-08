package com.example.pasajero.repository;

import com.example.pasajero.model.Pasajero;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface PasajeroRepository extends MongoRepository<Pasajero, String> {
}
