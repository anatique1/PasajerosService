package com.example.pasajero.repository;

import com.example.pasajero.model.CuentaGoogle;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface CuentaGoogleRepository extends MongoRepository<CuentaGoogle, String> {
}
